package com.daimler.github

class Github {
    // --- Constructor
    def context

    Github(context) {
        this.context = context
    }

    // --- Comment
    def buildComment(text) {
        if (context.env.CHANGE_ID) {
            def comment = findComment()

            if (comment) {
                context.pullRequest.editComment(comment.id, text)
            } else {
                context.pullRequest.comment(text)
            }
        }
    }

    private def findComment() {
        for (comment in context.pullRequest.comments) {
            if (comment.user.equals('we-like-team-play')) {
                return comment
            }
        }
        return null
    }
}
