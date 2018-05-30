/**
 * Created by m500288 on 19.02.15.
 */

describe("Test für Check", function() {

    beforeEach(function () {
        REC.init();
    });

    it("test1", function() {
        var rules = '  <check lowerValue="100" upperValue="300" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var result = new Result( "100", rules, 100, 0, 3, "int", "asd");
        var searchItem = new SearchItem({name: "Wert", objectTyp:"int"});
        searchItem.erg.addResult(result);
        var check = new Check(new XMLObject(XMLDoc.docNode), searchItem);
        check.resolve();
        expect(searchItem.erg[0].check).toBeTruthy();
        expect(searchItem.erg[0].error).toBeNull();
    });

    it("test2", function() {
        var rules = '  <check lowerValue="100" upperValue="300" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var result = new Result("99", rules, 99, 0, 3, "int", "asd");
        var searchItem = new SearchItem({name: "Wert", objectTyp:"int"});
        searchItem.erg.addResult(result);
        var check = new Check(new XMLObject(XMLDoc.docNode), searchItem);
        check.resolve();
        expect(searchItem.erg[0].check).not.toBeTruthy();
        expect( searchItem.erg[0].error).toBe("Wert maybe wrong [99] is smaller 100");
    });

    it("test3", function() {
        var rules = '  <check lowerValue="100" upperValue="300" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var result = new Result("301", rules, 301, 0, 3, "int", "asd");
        var searchItem = new SearchItem({name: "Wert", objectTyp:"int"});
        searchItem.erg.addResult(result);
        var check = new Check(new XMLObject(XMLDoc.docNode), searchItem);
        check.resolve();
        expect(searchItem.erg[0].check).not.toBeTruthy();
        expect(searchItem.erg[0].error).toBe("Wert maybe wrong [301] is bigger 300");
    });

    it("test4", function() {
        var rules = '  <check lowerValue="100" upperValue="300" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var result = new Result("300", rules, 300.00, 0, 3, "float", "asd");
        var searchItem = new SearchItem({name: "Wert", objectTyp:"float"});
        searchItem.erg.addResult(result);
        var check = new Check(new XMLObject(XMLDoc.docNode), searchItem);
        check.resolve();
        expect(searchItem.erg[0].check).toBeTruthy();
        expect(searchItem.erg[0].error).toBeNull();
    });

    it("test5", function() {
        var rules = '  <check lowerValue="100" upperValue="300" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var result = new Result("300", rules, 300.01, 0, 3, "float", "asd");
        var searchItem = new SearchItem({name: "Wert", objectTyp:"float"});
        searchItem.erg.addResult(result);
        var check = new Check(new XMLObject(XMLDoc.docNode), searchItem);
        check.resolve();
        expect(searchItem.erg[0].check).not.toBeTruthy();
        expect(searchItem.erg[0].error).toBe("Wert maybe wrong [300.01] is bigger 300");
    });

    it("test6", function() {
        var rules = '  <check lowerValue="01/02/2015" upperValue="12/31/2015" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var result = new Result("02.01.2015", rules, new Date(2015,0,2), 0, 3, "date", "asd");
        var searchItem = new SearchItem({name: "Wert", objectTyp:"date"});
        searchItem.erg.addResult(result);
        var check = new Check(new XMLObject(XMLDoc.docNode), searchItem);
        check.resolve();
        expect(searchItem.erg[0].check).toBeTruthy();
        expect(searchItem.erg[0].error).toBeNull();
    });

    it("test7", function() {
        var rules = '  <check lowerValue="01/02/2015" upperValue="12/31/2015" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var result = new Result("01.01.2015", rules, new Date(2015,0,1), 0, 3, "date", "asd");
        var searchItem = new SearchItem({name: "Wert", objectTyp:"date"});
        searchItem.erg.addResult(result);
        var check = new Check(new XMLObject(XMLDoc.docNode), searchItem);
        check.resolve();
        expect(searchItem.erg[0].check).not.toBeTruthy();
        expect(searchItem.erg[0].error).toBe("Wert maybe wrong [" + new Date(2015,0,1).toString() + "] is smaller " + new Date(2015,0,2).toString());
    });

    it("test8", function() {
        var rules = '  <check lowerValue="01/02/2015" upperValue="12/31/2015" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var result = new Result("01.01.2015", rules, new Date(2016,0,1), 0, 3, "date", "asd");
        var searchItem = new SearchItem({name: "Wert", objectTyp:"date"});
        searchItem.erg.addResult(result);
        var check = new Check(new XMLObject(XMLDoc.docNode), searchItem);
        check.resolve();
        expect(searchItem.erg[0].check).not.toBeTruthy();
        expect(searchItem.erg[0].error).toBe("Wert maybe wrong [" + new Date(2016,0,1).toString() + "] is bigger " + new Date(2015,11,31).toString());
    });


    it("test9", function() {
        var rules = '  <check lowerValue="b" upperValue="y" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var result = new Result( "k", rules, "k", 0, 3, "string", "asd");
        var searchItem = new SearchItem({name: "Wert"});
        searchItem.erg.addResult(result);
        var check = new Check(new XMLObject(XMLDoc.docNode), searchItem);
        check.resolve();
        expect(searchItem.erg[0].check).toBeTruthy();
        expect(searchItem.erg[0].error).toBeNull();
    });

    it("test10", function() {
        var rules = '  <check lowerValue="b" upperValue="y" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var result = new Result("a", rules, "a", 0, 3, "string", "asd");
        var searchItem = new SearchItem({name: "Wert"});
        searchItem.erg.addResult(result);
        var check = new Check(new XMLObject(XMLDoc.docNode), searchItem);
        check.resolve();
        expect(searchItem.erg[0].check).not.toBeTruthy();
        expect(searchItem.erg[0].error).toBe("Wert maybe wrong [a] is smaller b");
    });

    it("test11", function() {
        var rules = '  <check lowerValue="b" upperValue="y" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var result = new Result("z", rules, "z", 0, 3, "string", "asd");
        var searchItem = new SearchItem({name: "Wert"});
        searchItem.erg.addResult(result);
        var check = new Check(new XMLObject(XMLDoc.docNode), searchItem);
        check.resolve();
        expect(searchItem.erg[0].check).not.toBeTruthy();
        expect(searchItem.erg[0].error).toBe("Wert maybe wrong [z] is bigger y");
    });

});









