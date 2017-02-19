/**
 * Created by klaus on 09.02.2015.
 */

describe("Test für Delimitter", function() {

    beforeEach(function () {
        REC.init();
    });

    it("test1", function() {
        var text = "     Dies ist ein  Test";
        var erg = new SearchResultContainer();
        var result = new SearchResult(text, text, "Test", 0, text.length, "String", "asd");
        erg.addResult(result);
        var rules = '<delimitter typ="start" count="5" text="&#0032;" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var delimitter = new Delimitter(new XMLObject(XMLDoc.docNode));
        erg = delimitter.resolve(erg, false);
        expect(erg.getResult().text).toBe("Dies ist ein  Test");
        expect(erg.getResult().getStart()).toBe(5);
    });

    it("test2", function() {
        var text = "     Dies ist ein  Test";
        var erg = new SearchResultContainer();
        var result = new SearchResult(text, text, "Test", 0, text.length, "String", "asd");
        erg.addResult(result);
        var rules = '<delimitter debugLevel="trace" typ="start" count="6"  text="&#0032;" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var delimitter = new Delimitter(new XMLObject(XMLDoc.docNode));
        erg = delimitter.resolve(erg, false);
        expect(erg.getResult().text).toBe("ist ein  Test");
        expect(erg.getResult().getStart()).toBe(10);
    });

    it("test3", function() {
        var text = "\n \nDies ist ein Test";
        var erg = new SearchResultContainer();
        var result = new SearchResult(text, text, "Test", 0, text.length, "String", "asd");
        erg.addResult(result);
        var rules = '<delimitter debugLevel="trace" typ="start" count="2"  text="&#0010;"/>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var delimitter = new Delimitter(new XMLObject(XMLDoc.docNode));
        erg = delimitter.resolve(erg, false);
        expect(erg.getResult().text).toBe("Dies ist ein Test");
        expect(erg.getResult().getStart()).toBe(3);
    });

    it("test4", function() {
        var text = "Dies\nist\nein\nTest";
        var erg = new SearchResultContainer();
        var result = new SearchResult(text, text, "Test", 0, text.length, "String", "asd");
        erg.addResult(result);
        var rules = '<delimitter debugLevel="trace" typ="start" count="-3"  text="&#0010;"/>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var delimitter = new Delimitter(new XMLObject(XMLDoc.docNode));
        erg = delimitter.resolve(erg, false);
        expect(erg.getResult().text).toBe("ist\nein\nTest");
        expect(erg.getResult().getStart()).toBe(5);
    });

    it("test5", function() {
        var text = "Dies\nist\nein\nTest\n";
        var erg = new SearchResultContainer();
        var result = new SearchResult(text, text, "Test", 0, text.length, "String", "asd");
        erg.addResult(result);
        var rules = '<delimitter debugLevel="trace" typ="start" count="-3"  text="&#0010;"/>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var delimitter = new Delimitter(new XMLObject(XMLDoc.docNode));
        erg = delimitter.resolve(erg, false);
        expect(erg.getResult().text).toBe("ein\nTest\n");
        expect(erg.getResult().getStart()).toBe(9);
    });

    it("test6", function() {
        var text = "Dies ist ein Test";
        var erg = new SearchResultContainer();
        var result = new SearchResult(text, text, "Test", 0, text.length, "String", "asd");
        erg.addResult(result);
        var rules = '<delimitter debugLevel="trace" typ="start" count="3"  text="e"/>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var delimitter = new Delimitter(new XMLObject(XMLDoc.docNode));
        erg = delimitter.resolve(erg, false);
        expect(erg.getResult().text).toBe("st");
        expect(erg.getResult().getStart()).toBe(15);
    });

    it("test7", function() {
        var text = "Dies ist ein Test";
        var erg = new SearchResultContainer();
        var result = new SearchResult(text, text, "Test", 0, text.length, "String", "asd");
        erg.addResult(result);
        var rules = '<delimitter debugLevel="trace" typ="end" count="3"  text="e"/>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var delimitter = new Delimitter(new XMLObject(XMLDoc.docNode));
        erg = delimitter.resolve(erg, false);
        expect(erg.getResult().text).toBe("Dies ist ein T");
        expect(erg.getResult().getEnd()).toBe(14);
    });

    it("test8", function() {
        var text = "Dies ist ein Test";
        var erg = new SearchResultContainer();
        var result = new SearchResult(text, text, "Test", 0, text.length, "String", "asd");
        erg.addResult(result);
        var rules = '<delimitter debugLevel="trace" typ="end" count="2"  text="e"/>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var delimitter = new Delimitter(new XMLObject(XMLDoc.docNode));
        erg = delimitter.resolve(erg, false);
        expect(erg.getResult().text).toBe("Dies ist ");
        expect(erg.getResult().getEnd()).toBe(9);
    });

    it("test9", function() {
        var text = "Dies ist ein Test";
        var erg = new SearchResultContainer();
        var result = new SearchResult(text, text, "Test", 0, text.length, "String", "asd");
        erg.addResult(result);
        var rules = '<delimitter debugLevel="trace" typ="start" count="4" removeBlanks="before" text=""/>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var delimitter = new Delimitter(new XMLObject(XMLDoc.docNode));
        erg = delimitter.resolve(erg, false);
        expect(erg.getResult().text).toBe("isteinTest");
        expect(erg.getResult().getStart()).toBe(4);
    });

    it("test10", function() {
        var text = "Dies ist ein Test";
        var erg = new SearchResultContainer();
        var result = new SearchResult(text, text, "Test", 0, text.length, "String", "asd");
        erg.addResult(result);
        var rules = '<delimitter debugLevel="trace" typ="start" count="5" removeBlanks="after" text=""/>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var delimitter = new Delimitter(new XMLObject(XMLDoc.docNode));
        erg = delimitter.resolve(erg, false);
        expect(erg.getResult().text).toBe("isteinTest");
        expect(erg.getResult().getStart()).toBe(5);
    });

});









