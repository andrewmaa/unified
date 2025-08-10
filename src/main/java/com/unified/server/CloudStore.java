package com.unified.server;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.ServiceOptions;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.unified.model.Student;

import java.util.*;
import java.util.concurrent.ExecutionException;

/** Firestore access for Unified (hard-fail on errors). */
public final class CloudStore {

    private static final String ENV_PROJECT = "FIRESTORE_PROJECT_ID";
    private static final String projectId =
            Optional.ofNullable(System.getenv(ENV_PROJECT))
                    .orElse(ServiceOptions.getDefaultProjectId());

    private static final Firestore db = FirestoreOptions.newBuilder()
            .setProjectId(projectId)
            .setCredentials(getCredentials())
            .build()
            .getService();

    private CloudStore() {}

    private static GoogleCredentials getCredentials() {
        try { return GoogleCredentials.getApplicationDefault(); }
        catch (Exception e) {
            throw new IllegalStateException(
                    "ADC not found. Set GOOGLE_APPLICATION_CREDENTIALS or run on GCP with a service account.", e);
        }
    }

    public static String getProjectId() { return projectId; }
    public static Firestore getDb() { return db; }

    // ---------- Health check ----------
    public static HealthResult ping() {
        String id = "ping-" + System.currentTimeMillis();
        Map<String, Object> doc = Map.of("ok", true, "ts", Timestamp.now(), "projectId", projectId);
        try {
            db.collection("health").document(id).set(doc).get();
            var snap = db.collection("health").document(id).get().get();
            boolean ok = snap.exists() && Boolean.TRUE.equals(snap.getBoolean("ok"));
            return new HealthResult(ok, id, projectId, null);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return new HealthResult(false, id, projectId, String.valueOf(ie));
        } catch (ExecutionException ee) {
            return new HealthResult(false, id, projectId, String.valueOf(cause(ee)));
        }
    }

    // ---------- Users ----------
    /** Upsert student; docId prefers username then userId. */
    public static UpsertResult saveUser(Student s) {
        try {
            String docId = firstNonBlank(s.getUsername(), s.getUserId());
            if (docId == null) throw new IllegalArgumentException("username or userId required");

            Map<String, Object> doc = new LinkedHashMap<>();
            putIfNotNull(doc, "userId", s.getUserId());
            putIfNotNull(doc, "username", s.getUsername());
            putIfNotNull(doc, "fullName", s.getFullName());
            putIfNotNull(doc, "email", s.getEmail());
            putIfNotNull(doc, "studentId", s.getStudentId());
            // optional profile
            try { putIfNotNull(doc, "yearOfGraduation", String.valueOf(Student.class.getMethod("getYearOfGraduation").invoke(s))); } catch (Throwable ignore) {}
            try { putIfNotNull(doc, "major", String.valueOf(Student.class.getMethod("getMajor").invoke(s))); } catch (Throwable ignore) {}
            try { putIfNotNull(doc, "school", String.valueOf(Student.class.getMethod("getSchool").invoke(s))); } catch (Throwable ignore) {}
            try { Object courses = Student.class.getMethod("getEnrolledCourses").invoke(s); doc.put("enrolledCourses", normalizeValue(courses)); } catch (Throwable ignore) {}
            // password (hashed) if your model提供
            try { putIfNotNull(doc, "hashedPassword", Student.class.getMethod("getHashedPassword").invoke(s)); } catch (Throwable ignore) {}

            doc.put("online", Boolean.TRUE.equals(s.isOnline()));
            doc.put("updatedAt", Timestamp.now());

            WriteResult wr = db.collection("users").document(docId).set(normalizeMap(doc)).get();
            return new UpsertResult(true, docId, wr.getUpdateTime().toString(), null);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return new UpsertResult(false, null, null, String.valueOf(ie));
        } catch (ExecutionException ee) {
            return new UpsertResult(false, null, null, String.valueOf(cause(ee)));
        } catch (Exception e) {
            return new UpsertResult(false, null, null, String.valueOf(e));
        }
    }

    public static Student getStudentByUsername(String username) {
        if (isBlank(username)) return null;
        try {
            DocumentSnapshot snap = db.collection("users").document(username).get().get();
            if (!snap.exists()) {
                QuerySnapshot qs = db.collection("users").whereEqualTo("username", username).limit(1).get().get();
                if (qs.isEmpty()) return null;
                snap = qs.getDocuments().get(0);
            }
            Map<String,Object> m = snap.getData();
            if (m == null) return null;
            return new Student(
                    str(m.get("userId")),
                    str(m.get("username")),
                    str(m.get("fullName")),
                    str(m.get("email")),
                    str(m.get("hashedPassword")),
                    str(m.get("studentId"))
            );
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ie);
        } catch (ExecutionException ee) {
            throw new RuntimeException(cause(ee));
        }
    }

    // ---------- Channels ----------
    public static List<Map<String, Object>> listChannelsByUser(String userId) {
        if (isBlank(userId)) return List.of();
        try {
            List<Map<String, Object>> out = new ArrayList<>();
            ApiFuture<QuerySnapshot> q1 = db.collection("channels").whereArrayContains("participants", userId).get();
            ApiFuture<QuerySnapshot> q2 = db.collection("channels").whereEqualTo("ownerId", userId).get();

            for (QueryDocumentSnapshot d : q1.get().getDocuments()) out.add(row(d));
            for (QueryDocumentSnapshot d : q2.get().getDocuments())
                if (out.stream().noneMatch(x -> Objects.equals(x.get("channelId"), d.getId()))) out.add(row(d));

            out.sort((a,b) -> Long.compare(ts(b.get("updatedAt")), ts(a.get("updatedAt"))));
            return out;
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ie);
        } catch (ExecutionException ee) {
            throw new RuntimeException(cause(ee));
        }
    }

    public static String createChannel(Map<String, Object> doc) {
        if (doc == null) doc = new LinkedHashMap<>();
        Object parts = doc.getOrDefault("participants", new ArrayList<>());
        if (!(parts instanceof List)) parts = new ArrayList<>((Collection<?>) parts);
        @SuppressWarnings("unchecked") List<Object> p = (List<Object>) parts;
    
        String ownerId = str(doc.get("ownerId"));
        if (ownerId != null && p.stream().noneMatch(v -> Objects.equals(String.valueOf(v), ownerId))) {
            p.add(ownerId);
        }
    
        doc.put("participants", normalizeValue(p));
        doc.putIfAbsent("createdAt", Timestamp.now());
        doc.put("updatedAt", Timestamp.now());
    
        String forcedId = str(doc.get("channelId")); // 允许外部指定
        try {
            DocumentReference ref = (forcedId != null && !forcedId.isBlank())
                    ? db.collection("channels").document(forcedId)
                    : db.collection("channels").document(); // auto id
            ref.set(normalizeMap(doc)).get();
            return ref.getId();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ie);
        } catch (ExecutionException ee) {
            throw new RuntimeException(cause(ee));
        }
    }
    public static Map<String,Object> findChannelByExactName(String name){
        try {
            var qs = db.collection("channels")
                    .whereEqualTo("name", name).limit(1).get().get();
            if (qs.isEmpty()) return null;
            var d = qs.getDocuments().get(0);
            return row(d);
        } catch (Exception e) { throw new RuntimeException(e); }
    }
    
    public static List<Map<String,Object>> searchChannelsByPrefix(String prefix){
        try {
            String end = prefix + "\uf8ff";
            var qs = db.collection("channels")
                    .whereGreaterThanOrEqualTo("name", prefix)
                    .whereLessThan("name", end).get().get();
            List<Map<String,Object>> out = new ArrayList<>();
            for (var d : qs.getDocuments()) out.add(row(d));
            return out;
        } catch (Exception e) { throw new RuntimeException(e); }
    }
    
    public static void addParticipant(String channelId, String userId) {
        try {
            db.collection("channels").document(channelId)
              .update("participants", FieldValue.arrayUnion(userId)).get();
            db.collection("channels").document(channelId)
              .set(Map.of("updatedAt", Timestamp.now()), SetOptions.merge()).get();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ie);
        } catch (ExecutionException ee) {
            throw new RuntimeException(cause(ee));
        }
    }
    
    

    // ---------- Messages ----------
    public static List<Map<String, Object>> listMessages(String channelId) {
        if (isBlank(channelId)) return List.of();
        try {
            List<Map<String, Object>> out = new ArrayList<>();
            QuerySnapshot qs = db.collection("channels").document(channelId)
                    .collection("messages").orderBy("createdAt", Query.Direction.ASCENDING).get().get();
            for (QueryDocumentSnapshot d : qs.getDocuments()) {
                Map<String,Object> m = new LinkedHashMap<>(d.getData());
                m.put("messageId", d.getId());
                out.add(m);
            }
            return out;
        } catch (InterruptedException ie) { Thread.currentThread().interrupt(); throw new RuntimeException(ie); }
        catch (ExecutionException ee) { throw new RuntimeException(cause(ee)); }
    }

    public static String addMessage(String channelId, Map<String, Object> msgDoc) {
        if (isBlank(channelId)) throw new IllegalArgumentException("channelId required");
        if (msgDoc == null) msgDoc = new LinkedHashMap<>();
        msgDoc.putIfAbsent("type", "text");
        msgDoc.putIfAbsent("createdAt", Timestamp.now());
        try {
            DocumentReference msgRef = db.collection("channels").document(channelId)
                    .collection("messages").add(normalizeMap(msgDoc)).get();
            db.collection("channels").document(channelId)
                    .set(Map.of("updatedAt", Timestamp.now(),
                            "lastMessageSummary", String.valueOf(msgDoc.getOrDefault("content","")).substring(0,
                                    Math.min(120, String.valueOf(msgDoc.getOrDefault("content","")).length()))),
                            SetOptions.merge());
            return msgRef.getId();
        } catch (InterruptedException ie) { Thread.currentThread().interrupt(); throw new RuntimeException(ie); }
        catch (ExecutionException ee) { throw new RuntimeException(cause(ee)); }
    }

    // ---------- helpers ----------
    public static final class UpsertResult {
        public final boolean ok; public final String id; public final String updateTime; public final String error;
        public UpsertResult(boolean ok, String id, String updateTime, String error) {
            this.ok=ok; this.id=id; this.updateTime=updateTime; this.error=error;
        }
    }
    private static long ts(Object ts){
        if (ts instanceof Timestamp) {
            return ((Timestamp) ts).toSqlTimestamp().getTime();
        }
        return 0L;
    }
    
    public static final class HealthResult {
        public final boolean ok; public final String id; public final String projectId; public final String error;
        public HealthResult(boolean ok, String id, String projectId, String error) {
            this.ok=ok; this.id=id; this.projectId=projectId; this.error=error;
        }
    }

    private static Map<String,Object> row(QueryDocumentSnapshot d){
        Map<String,Object> m = new LinkedHashMap<>(d.getData());
        m.put("channelId", d.getId());
        return m;
    }
    private static int cmpTs(Object ts){
        if (ts instanceof Timestamp) return (int)((Timestamp)ts).toSqlTimestamp().getTime();
        return 0;
    }
    private static void putIfNotNull(Map<String, Object> m, String k, Object v){ if(k!=null && v!=null) m.put(k,v); }
    private static String str(Object o){ return o==null?null:String.valueOf(o); }
    private static boolean isBlank(String s){ return s==null || s.isBlank(); }
    private static String firstNonBlank(String... a){ for(String s:a) if(!isBlank(s)) return s; return null; }
    private static Throwable cause(ExecutionException ee){ return ee.getCause()!=null?ee.getCause():ee; }

    @SuppressWarnings("unchecked")
    private static Object normalizeValue(Object v) {
        if (v == null) return null;
        if (v.getClass().isArray()) {
            int n = java.lang.reflect.Array.getLength(v);
            List<Object> out = new ArrayList<>(n);
            for (int i=0;i<n;i++) out.add(normalizeValue(java.lang.reflect.Array.get(v,i)));
            return out;
        }
        if (v instanceof Collection) {
            List<Object> out = new ArrayList<>();
            for (Object it : (Collection<?>) v) out.add(normalizeValue(it));
            return out;
        }
        if (v instanceof Map) {
            Map<String,Object> out = new LinkedHashMap<>();
            ((Map<?,?>)v).forEach((k,val)->{ if(k!=null) out.put(String.valueOf(k), normalizeValue(val));});
            return out;
        }
        return v;
    }
    private static Map<String,Object> normalizeMap(Map<String,Object> m){
        Map<String,Object> out = new LinkedHashMap<>();
        for (var e: m.entrySet()) if (e.getKey()!=null) out.put(e.getKey(), normalizeValue(e.getValue()));
        return out;
    }
}
