/**
 * Created by m500288 on 19.02.15.
 */

describe("Test für Format", function() {

    beforeEach(function () {
        REC.init();
    });

    it("test1", function() {
        var rules = ' <format formatString="YYYY" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var format = new Format(new XMLObject(XMLDoc.docNode));
        erg = format.resolve(new Date(2015, 0 ,1));
        expect(erg).toBe("2015");
    });

    it("test2", function() {
        var rules = '<format formatString="MMMM YYYY" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var format = new Format(new XMLObject(XMLDoc.docNode));
        erg = format.resolve(new Date(2015, 0 ,1));
        expect(erg).toBe("Januar 2015");
    });

    it("test3", function() {
        var rules = '<format formatString="MMM YYYY" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var format = new Format(new XMLObject(XMLDoc.docNode));
        erg = format.resolve(new Date(2015, 0 ,1));
        expect(erg).toBe("Jan 2015");
    });


    it("test4", function() {
        var rules = ' <format formatString="d.MM.YYYY" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var format = new Format(new XMLObject(XMLDoc.docNode));
        erg = format.resolve(new Date(2015, 0 ,1));
        expect(erg).toBe("01.01.2015");
    });

    it("test5", function() {
        var rules = ' <format formatString="###" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var format = new Format(new XMLObject(XMLDoc.docNode));
        erg = format.resolve(2);
        expect(erg).toBe("2");
    });

    it("test6", function() {
        var rules = ' <format formatString="000" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var format = new Format(new XMLObject(XMLDoc.docNode));
        erg = format.resolve(2);
        expect(erg).toBe("002");
    });

    it("test7", function() {
        var rules = ' <format formatString="000,00" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var format = new Format(new XMLObject(XMLDoc.docNode));
        erg = format.resolve(2);
        expect(erg).toBe("002,00");
    });

    it("test8", function() {
        var rules = ' <format formatString="000,00" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var format = new Format(new XMLObject(XMLDoc.docNode));
        erg = format.resolve(2.1);
        expect(erg).toBe("002,10");
    });

    it("test9", function() {
        var rules = ' <format formatString="0.000,00" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var format = new Format(new XMLObject(XMLDoc.docNode));
        erg = format.resolve(2257.1);
        expect(erg).toBe("2.257,10");
    });
});









