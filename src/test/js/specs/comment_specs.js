/**
 * Created by klaus on 22.03.2015.
 */

describe("Test für Kommentare", function() {

    beforeEach(function(){
        REC.init();
        var iBox = companyhome.childByNamePath("/Archiv/Inbox");
        REC.currentDocument = iBox.createNode("WebScriptTest", "my:archivContent");
        REC.currentDocument.setProperty("cm:title", "Test Title");
        REC.currentDocument.setProperty("my:person", "Klaus");
        REC.currentDocument.properties.content.write(new Content("Test"));
    });

    it("Teste addComment", function () {
        var comments = new Comments();
        comments.addComment(REC.currentDocument, "Test");
        expect(REC.currentDocument.hasAspect("fm:discussable").toBeTruthy);
        var forumFolder = REC.currentDocument.childAssocs["fm:discussion"][0];
        expect(forumFolder).toBeTruthy();
        var topic = forumFolder.children[0];
        expect(topic).toBeTruthy();
        var comment = topic.childAssocs["cm:contains"][0];
        expect(comment).toBeTruthy();
        expect(comment.content).toBe("Test");
    });

    it("Teste removeComment", function () {
        var comments = new Comments();
        comments.addComment(REC.currentDocument, "<table border=\"1\"> <tr><td>Nummer</td><td>Fehler</td></tr> ");
        comments.removeComments(REC.currentDocument);
        expect(REC.currentDocument.hasAspect("fm:discussable")).toBeTruthy();
        var discussion = REC.currentDocument.childAssocs["fm:discussion"][0];
        expect(discussion).toBeTruthy();
        var topic = discussion.children[0];
        expect(topic).toBeTruthy();
        expect(topic.childAssocs["cm:contains"]).not.toBeDefined();
    });

});

