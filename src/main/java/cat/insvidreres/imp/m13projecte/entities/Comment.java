package cat.insvidreres.imp.m13projecte.entities;

public class Comment {

    private CommentInstance[] comments;

    static class CommentInstance {
        private String docName;
        private String userId;
        private String comment;

        public String getDocName() {
            return docName;
        }

        public void setDocName(String docName) {
            this.docName = docName;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }

}
