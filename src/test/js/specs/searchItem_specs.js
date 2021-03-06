
describe("Test für SearchItem", function() {

    beforeEach(function () {
        REC.init();
        var iBox = companyhome.childByNamePath("/Archiv/Inbox");
        REC.currentDocument = iBox.createNode("WebScriptTest", "my:archivContent");
        var txt = " Dies ist ein Test!Datum: 01.05.1965\r\n" +
            "Wert:\r\n" +
            " \r\n" +
            "21,65\r\n" +
            " \r\n" +
            "Datum\r\n" +
            "Datum: März 15 ID-Value  21\t22 Euro\t23\r\n" +
            "   \r\n" +
            "06.04.09\r\n" +
            "   \r\n" +
            "   \r\n" +
            "   \r\n" +
            "Nachtrag zum\r\n" +
            "22 März 2012   \r\n" +
            "Gesamt in EUR \r\n" +
            "950,56 \r\n" +
            "    \r\n" +
            "+21,49 \r\n" +
            "Wert 123,5\r\n" +
            "Gültig     10.März 2012     \r\n" +
            "24.12.2010 \r\n" +
            "KUSA Nr. 43124431\r\n" +
            "7. Januar 2008  \r\n" +
            "Rechnungsdatum23.08.2011\r\n" +
            "In den nächsten Tagen buchen wir 349,10 EUR von Ihrem Konto 123\r\n" +
            "Datum 21. März 2009 \r\n" +
            "Rechnungsbetrag 'ue 189.13 € \r\n" +
            "270 646 2793 \r\n" +
            "959 622 2280 \r\n" +
            "560 525 3966 \r\n" +
            "4.300,01 H \r\n" +
            "300 H \r\n" +
            "Der Verbrauch ist hoch.\r\n" +
            "Betrag dankend erhalten 302,26 €\r\n" +
            "Unsere Lieferungen\r\n" +
            "Zahlbetrag \r\n" +
            "	 25,65 € ";
        REC.currentDocument.properties.content.write(txt);
        Verteilung.positions.clear();
    });


    it("testResolveSearchItemWithFix", function() {
        var rules = '<searchItem name="Titel" fix="Test für Titel" target="cm:title" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        expect(searchItem.resolve()).toBe("Test für Titel");
    });

    it("testResolveSearchItemWithEval", function() {
        var rules = '<searchItem name="Datum" eval="new Date(2012,01,01)"  objectTyp="date" target="my:documentDate" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        expect(searchItem.resolve().getTime()).toBe(new Date(2012, 1, 1).getTime());
    });

    it("testResolveSearchItemWithEvalAndFormat", function() {
        var rules = '<searchItem name="Datum" eval="new Date(2012,01,01)" objectTyp="date" target="my:documentDate" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        REC.currentSearchItems = REC.currentSearchItems.concat(searchItem);
        rules = '<searchItem name="id" value="Datum" objectTyp="date"> <format formatString="YYYY" /> </searchItem>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        expect(searchItem.resolve()).toBe("2012");
    });


    it("testResolveSearchItemWithReadOverReturn", function() {
        var rules = '<searchItem name="Test 1" text="Datum" word="1" readOverReturn="true" objectTyp="date" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        expect(searchItem.resolve().getTime()).toBe(new Date(1965, 4, 1).getTime());
        var p = Verteilung.positions.get("Test 1");
         p.convertPosition(REC.getContent(REC.currentDocument));
        expect(p.startRow).toBe(0);
        expect(p.endRow).toBe(0);
        expect(p.startPosition).toBe(26);
        expect(p.endPosition).toBe(36);
    });


    it("testResolveSearchItemWithWordAndDate", function() {
        var rules = '<searchItem name="Test 3" text="ID-Value" word="1,2" direction="left" objectTyp="date" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        expect(searchItem.resolve().getTime()).toBe(new Date(2015, 2, 1).getTime());
        expect(Verteilung.positions).not.toBe(null);
        var p = Verteilung.positions.get("Test 3");
         p.convertPosition(REC.getContent(REC.currentDocument));
        expect(p.startRow).toBe(6);
        expect(p.endRow).toBe(6);
        expect(p.startColumn).toBe(7);
        expect(p.endColumn).toBe(14);
    });

    it("testResolveSearchItemWithWordAndFloat", function() {
        var rules = '<searchItem name="Test 4" text="Wert" word="1" readOverReturn="true" objectTyp="float" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        expect(searchItem.resolve()).toBe(21.65);
        expect(Verteilung.positions).not.toBe(null);
        var p = Verteilung.positions.get("Test 4");
         p.convertPosition(REC.getContent(REC.currentDocument));
        expect(p.startRow).toBe(3);
        expect(p.endRow).toBe(3);
        expect(p.startColumn).toBe(0);
        expect(p.endColumn).toBe(5);
    });

    it("testResolveSearchItemWithDelimitterAndFloat", function() {
        var rules = '<searchItem name="Test 5" text="Datum" readOverReturn="true" direction="left" objectTyp="float">' +
            '<delimitter typ="start" count="-3" text="{$RETURN}" />' +
            '<delimitter typ="end" count="1" text="&#0013;&#0010;" />' +
            '</searchItem>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        expect(searchItem.resolve()).toBe(21.65);
        expect(Verteilung.positions).not.toBe(null);
        var p = Verteilung.positions.get("Test 5");
         p.convertPosition(REC.getContent(REC.currentDocument));
        expect(p.startRow).toBe(3);
        expect(p.endRow).toBe(3);
        expect(p.startColumn).toBe(0);
        expect(p.endColumn).toBe(5);
    });

    it("testResolveSearchItemWithDelimitterAndInt", function() {
        var rules = '<searchItem name="Test 6" text="ID-Value" objectTyp="int">' +
            '<delimitter typ="start" count="2" text="&#0032;" />' +
            '<delimitter typ="end" count="1 "text="&#0009;" />' +
            '</searchItem>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        expect(searchItem.resolve()).toBe(21);
        expect(Verteilung.positions).not.toBe(null);
        var p = Verteilung.positions.get("Test 6");
         p.convertPosition(REC.getContent(REC.currentDocument));
        expect(p.startRow).toBe(6);
        expect(p.endRow).toBe(6);
        expect(p.startColumn).toBe(25);
        expect(p.endColumn).toBe(27);
    });


    it("testResolveSearchItemWithDelimitterAndInt", function() {
        var rules = '<searchItem name="Test 6" text="ID-Value" objectTyp="int">' +
            '<delimitter typ="start" count="2" text="{$BLANK}" />' +
            '<delimitter typ="end" count="1 "text="{$TAB}" />' +
            '</searchItem>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        expect(searchItem.resolve()).toBe(21);
        expect(Verteilung.positions).not.toBe(null);
        var p = Verteilung.positions.get("Test 6");
         p.convertPosition(REC.getContent(REC.currentDocument));
        expect(p.startRow).toBe(6);
        expect(p.endRow).toBe(6);
        expect(p.startColumn).toBe(25);
        expect(p.endColumn).toBe(27);
    });


    it("testResolveSearchItemWithDelimitterAndDate", function() {
        var rules = '<searchItem name="Test 7" text="Nachtrag" objectTyp="date" >' +
            '<delimitter typ="start" count="1" text="&#0010;" />' +
            '</searchItem>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        expect(searchItem.resolve().getTime()).toBe(new Date(2012, 2, 22).getTime());
        expect(Verteilung.positions).not.toBe(null);
        var p = Verteilung.positions.get("Test 7");
         p.convertPosition(REC.getContent(REC.currentDocument));
        expect(p.startRow).toBe(13);
        expect(p.endRow).toBe(13);
        expect(p.startColumn).toBe(0);
        expect(p.endColumn).toBe(12);
    });

    it("testResolveSearchItemWithFloat", function() {
        var rules = '<searchItem name="Test 8" text="Wert" objectTyp="float" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        expect(searchItem.resolve()).toBe(123.5);
        expect(Verteilung.positions).not.toBe(null);
        var p = Verteilung.positions.get("Test 8");
         p.convertPosition(REC.getContent(REC.currentDocument));
        expect(p.startRow).toBe(18);
        expect(p.endRow).toBe(18);
        expect(p.startColumn).toBe(5);
        expect(p.endColumn).toBe(10);
    });


    it("testResolveSearchItemWithDate", function() {
        var rules = '<searchItem name="Test 9" text="Gültig" objectTyp="date" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        expect(searchItem.resolve().getTime()).toBe(new Date(2012, 2, 10).getTime());
        expect(Verteilung.positions).not.toBe(null);
        var p = Verteilung.positions.get("Test 9");
         p.convertPosition(REC.getContent(REC.currentDocument));
        expect(p.startRow).toBe(19);
        expect(p.endRow).toBe(19);
        expect(p.startColumn).toBe(11);
        expect(p.endColumn).toBe(23);
    });

    it("testResolveSearchItemWithKindAndAmount", function() {
        var rules = '<searchItem name="Test 10" kind="amount,1" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        expect(searchItem.resolve()).toBe(22);
        expect(Verteilung.positions).not.toBe(null);
        var p = Verteilung.positions.get("Test 10");
         p.convertPosition(REC.getContent(REC.currentDocument));
        expect(p.startRow).toBe(6);
        expect(p.endRow).toBe(6);
        expect(p.startColumn).toBe(28);
        expect(p.endColumn).toBe(30);
    });

    it("testResolveSearchItemWithKindAndDate", function() {
        var rules = '<searchItem name="Test 11" kind="date,4" direction="left"/>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        expect(searchItem.resolve().getTime()).toBe(new Date(2010, 11, 24).getTime());
        expect(Verteilung.positions).not.toBe(null);
        var p = Verteilung.positions.get("Test 11");
         p.convertPosition(REC.getContent(REC.currentDocument));
        expect(p.startRow).toBe(20);
        expect(p.endRow).toBe(20);
        expect(p.startColumn).toBe(0);
        expect(p.endColumn).toBe(10);
    });

    it("testResolveSearchItemWithCheck", function() {
        var rules = '<searchItem name="Test 12" text="KUSA" objectTyp="date">' +
            '<delimitter typ="start" count="1" text="&#0010;" />' +
            '<check lowerValue="01/01/2005" upperValue="01/01/2020" />' +
            '</searchItem>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        expect(searchItem.resolve().getTime()).toBe(new Date(2008, 0, 7).getTime());
        expect(Verteilung.positions).not.toBe(null);
        var p = Verteilung.positions.get("Test 12");
         p.convertPosition(REC.getContent(REC.currentDocument));
        expect(p.startRow).toBe(22);
        expect(p.endRow).toBe(22);
        expect(p.startColumn).toBe(0);
        expect(p.endColumn).toBe(14);
    });

    it("testResolveSearchItem16", function() {
        var rules = '<searchItem name="Test 13" text="buchen" word="2" objectTyp="float" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        expect(searchItem.resolve()).toBe(349.10);
        expect(Verteilung.positions).not.toBe(null);
        var p = Verteilung.positions.get("Test 13");
         p.convertPosition(REC.getContent(REC.currentDocument));
        expect(p.startRow).toBe(24);
        expect(p.endRow).toBe(24);
        expect(p.startColumn).toBe(33);
        expect(p.endColumn).toBe(39);
    });

    it("testResolveSearchItemWithDirectionLeft", function() {
        var rules = '<searchItem name="Test 14" text="buchen" word="2" direction="left" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        expect(searchItem.resolve()).toBe('nächsten');
        expect(Verteilung.positions).not.toBe(null);
        var p = Verteilung.positions.get("Test 14");
         p.convertPosition(REC.getContent(REC.currentDocument));
        expect(p.startRow).toBe(24);
        expect(p.endRow).toBe(24);
        expect(p.startColumn).toBe(7);
        expect(p.endColumn).toBe(15);
    });

    it("testResolveSearchItem18", function() {
        var rules = '<searchItem name="Test 15" text="Konto" required="false" objectTyp="int" word="1">' +
            '<check lowerValue="123" />' +
            ' </searchItem>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        expect(searchItem.resolve()).toBe(123);
        expect(Verteilung.positions).not.toBe(null);
        var p = Verteilung.positions.get("Test 15");
         p.convertPosition(REC.getContent(REC.currentDocument));
        expect(p.startRow).toBe(24);
        expect(p.endRow).toBe(24);
        expect(p.startColumn).toBe(60);
        expect(p.endColumn).toBe(63);
    });


    it("testResolveSearchItemWithDateAndWithoutSpace", function() {
        var rules = '<searchItem name="Test 16" text="Rechnungsdatum" objectTyp="date" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        expect(searchItem.resolve().getTime()).toBe(new Date(2011, 7, 23).getTime());
        expect(Verteilung.positions).not.toBe(null);
        var p = Verteilung.positions.get("Test 16");
         p.convertPosition(REC.getContent(REC.currentDocument));
        expect(p.startRow).toBe(23);
        expect(p.endRow).toBe(23);
        expect(p.startColumn).toBe(14);
        expect(p.endColumn).toBe(24);
    });

    it("testResolveSearchItem20", function() {
        var rules = '<searchItem name="Test 17" text="Gesamt in EUR" objectTyp="float" word="2" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        expect(searchItem.resolve()).toBe(21.49);
        expect(Verteilung.positions).not.toBe(null);
        var p = Verteilung.positions.get("Test 17");
         p.convertPosition(REC.getContent(REC.currentDocument));
        expect(p.startRow).toBe(17);
        expect(p.endRow).toBe(17);
        expect(p.startColumn).toBe(0);
        expect(p.endColumn).toBe(6);
    });


    it("testResolveSearchItem21", function() {
        var rules = '<searchItem name="Test 18" kind="date,1" direction="left" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        expect(searchItem.resolve().getTime()).toBe(new Date(2009, 2, 21).getTime());
        expect(Verteilung.positions).not.toBe(null);
        var p = Verteilung.positions.get("Test 18");
         p.convertPosition(REC.getContent(REC.currentDocument));
        expect(p.startRow).toBe(25);
        expect(p.endRow).toBe(25);
        expect(p.startColumn).toBe(6);
        expect(p.endColumn).toBe(19);
    });

    it("testResolveSearchItem22", function() {
        var rules = '<searchItem name="txt" text="Rechnungsbetrag" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        REC.currentSearchItems = REC.currentSearchItems.concat(searchItem);
        rules = '<searchItem name="Test 19" value="txt" kind="amount" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        expect(searchItem.resolve()).toBe(189.13);
        expect(Verteilung.positions).not.toBe(null);
        var p = Verteilung.positions.get("Test 19");
         p.convertPosition(REC.getContent(REC.currentDocument));
        expect(p).not.toBe(null);
        expect(p.startRow).toBe(26);
        expect(p.endRow).toBe(26);
        expect(p.startColumn).toBe(20);
        expect(p.endColumn).toBe(26);
    });

    it("testResolveSearchItem23", function() {
        var rules = '<searchItem name="txt" text="Rechnungsbetrag" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        REC.currentSearchItems = REC.currentSearchItems.concat(searchItem);
        rules = '<searchItem name="Test 19" value="txt" kind="amount" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        REC.currentSearchItems = REC.currentSearchItems.concat(searchItem);
        rules = '<searchItem name="Test 20" eval="{Test 19} * 2"/>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        expect(searchItem.resolve()).toBe(378.26);
    });

    it("testResolveSearchItem24", function() {
        var rules = '<searchItem name="bet1" text="Wertentwicklung in EUR" word="1" objectTyp="float" required="false" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        REC.currentSearchItems = REC.currentSearchItems.concat(searchItem);
        rules = '<searchItem name="bet2" text="Gesamt in EUR" word="2" objectTyp="float" required="false" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        REC.currentSearchItems = REC.currentSearchItems.concat(searchItem);
        rules = '<searchItem name="Test 21" eval="{bet1}||{bet2}" objectTyp="float"/>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        expect(searchItem.resolve()).toBe(21.49);
    });

    it("testResolveSearchItem25", function() {
        var rules = '<searchItem name="Test 22" text="XYZ|Gesamt" word="2" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        expect(searchItem.resolve()).toBe("EUR");
        expect(Verteilung.positions).not.toBe(null);
        var p = Verteilung.positions.get("Test 22");
         p.convertPosition(REC.getContent(REC.currentDocument));
        expect(p.startRow).toBe(14);
        expect(p.endRow).toBe(14);
        expect(p.startColumn).toBe(10);
        expect(p.endColumn).toBe(13);
    });

    it("testResolveSearchItem26", function() {
        var rules = '<searchItem name="Test 23" text="960|959" included="true" word="0,3" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        expect(searchItem.resolve()).toBe('959 622 2280');
        expect(Verteilung.positions).not.toBe(null);
        var p = Verteilung.positions.get("Test 23");
         p.convertPosition(REC.getContent(REC.currentDocument));
        expect(p.startRow).toBe(28);
        expect(p.endRow).toBe(28);
        expect(p.startColumn).toBe(0);
        expect(p.endColumn).toBe(12);
    });


    it("testResolveSearchItem27", function() {
        var rules = '<searchItem name="Test 24" text="3966" included="true" word="0,3" direction="left" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        expect(searchItem.resolve()).toBe('560 525 3966');
        expect(Verteilung.positions).not.toBe(null);
        var p = Verteilung.positions.get("Test 24");
         p.convertPosition(REC.getContent(REC.currentDocument));
        expect(p.startRow).toBe(29);
        expect(p.endRow).toBe(29);
        expect(p.startColumn).toBe(0);
        expect(p.endColumn).toBe(12);
    });


    it("testResolveSearchItem28", function() {
        var rules = '<searchItem name="Test 25" text="H " objectTyp="float" readOverReturn="true" word="1" direction="left" backwards="true" >' +
            '<check lowerValue="400" upperValue="10000" />' +
            '</searchItem>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        expect(searchItem.resolve()).toBe(4300.01);
        expect(Verteilung.positions).not.toBe(null);
        var p = Verteilung.positions.get("Test 25");
         p.convertPosition(REC.getContent(REC.currentDocument));
        expect(p.startRow).toBe(30);
        expect(p.endRow).toBe(30);
        expect(p.startColumn).toBe(0);
        expect(p.endColumn).toBe(8);
    });

    it("testResolveSearchItem29", function() {
        var rules = '<searchItem name="txt" text="Zahlbetrag" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        REC.currentSearchItems = REC.currentSearchItems.concat(searchItem);
        rules = '<searchItem name="Test 19" value="txt" kind="amount" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        expect(searchItem.resolve()).toBe(undefined);
    });

    it("testResolveSearchItemWithPositionAndInt", function() {
        var rules = ' <searchItem name="betrag" text="erhalten" readOverReturn="true" objectTyp="int" target="my:amount" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        expect(searchItem.resolve()).toBe(302);
        expect(Verteilung.positions).not.toBe(null);
        var p = Verteilung.positions.get("betrag");
         p.convertPosition(REC.getContent(REC.currentDocument));
        expect(p.startRow).toBe(33);
        expect(p.endRow).toBe(33);
        expect(p.startColumn).toBe(24);
        expect(p.endColumn).toBe(27);
    });

    it("testResolveSearchItemWithPositionAndFloat", function() {
        var rules = ' <searchItem name="betrag" text="erhalten" readOverReturn="true" objectTyp="float" target="my:amount" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        expect(searchItem.resolve()).toBe(302.26);
        expect(Verteilung.positions).not.toBe(null);
        var p = Verteilung.positions.get("betrag");
         p.convertPosition(REC.getContent(REC.currentDocument));
        expect(p.startRow).toBe(33);
        expect(p.endRow).toBe(33);
        expect(p.startColumn).toBe(24);
        expect(p.endColumn).toBe(30);
    });

    it("testFindForWord1", function() {
        var text = "Dies ist ein Test";
        var result = new Result(text, "", "Test", 0, text.length, "String", "asd");
        var searchItem = new SearchItem({});
        searchItem.erg.addResult(result);
        searchItem.findForWords( [1], false);
        expect(searchItem.erg.getResult().text).toBe("ist");
        expect(searchItem.erg.getResult().getStart()).toBe(5);
        expect(searchItem.erg.getResult().getEnd()).toBe(8);
    });

    it("testFindForWord2", function() {
        var text = "Dies ist ein Test";
        var result = new Result( text, "", "Test", 0, text.length, "String", "asd");
        var searchItem = new SearchItem({});
        searchItem.erg.addResult(result);
        searchItem.findForWords([1,2], false);
        expect(searchItem.erg.getResult().text).toBe("ist ein");
        expect(searchItem.erg.getResult().getStart()).toBe(5);
        expect(searchItem.erg.getResult().getEnd()).toBe(12);
    });

    it("testFindForWord3", function() {
        var text = "Dies ist ein Test";
        var erg = new ResultContainer();
        var result = new Result(text, "", "Test", 0, text.length, "String", "asd");
        var searchItem = new SearchItem({});
        searchItem.erg.addResult(result);
        searchItem.findForWords([2,2], true);
        expect(searchItem.erg.getResult().text).toBe("Dies ist");
        expect(searchItem.erg.getResult().getStart()).toBe(0);
        expect(searchItem.erg.getResult().getEnd()).toBe(8);
    });

    it("testFindForSpecialType1", function() {
        var text = "Dies 01.01.2010 ist hoffentlich ein Datum";
        var searchItem = new SearchItem({});
        var erg =  searchItem.findSpecialType(text, ["date"], false, null);
        expect(erg[0].text).toBe("01.01.2010");
        expect(erg[0].getStart()).toBe(5);
        expect(erg[0].getEnd()).toBe(15);
        expect(erg[0].val.getTime()).toBe(new Date(2010,0,1).getTime());
    });

    it("testFindForSpecialType2", function() {
        var text = "Dies 125,78 €ist hoffentlich ein Betrag";
        var searchItem = new SearchItem({});
        var erg =  searchItem.findSpecialType(text, ["amount"], false, null);
        expect(erg[0].text).toBe("125,78 €");
        expect(erg[0].getStart()).toBe(5);
        expect(erg[0].getEnd()).toBe(11);
        expect(erg[0].val).toBe(125.78);
    });

    it("testFindForSpecialType3", function() {
        var text = "Dies 125,78 €ist hoffentlich ein Betrag";
        var searchItem = new SearchItem({});
        var erg =  searchItem.findSpecialType(text, ["float"], false, null);
        expect(erg[0].text).toBe("125,78");
        expect(erg[0].getStart()).toBe(5);
        expect(erg[0].getEnd()).toBe(11);
        expect(erg[0].val).toBe(125.78);
    });


    it("testFindForSpecialType4", function() {
        var text = "Dies 01. März 2010 ist hoffentlich ein Datum";
        var searchItem = new SearchItem({});
        var erg =  searchItem.findSpecialType(text, ["date"], false, null);
        expect(erg[0].text).toBe("01. März 2010");
        expect(erg[0].getStart()).toBe(5);
        expect(erg[0].getEnd()).toBe(18);
        expect(erg[0].val.getTime()).toBe(new Date(2010,2,1).getTime());
    });


    it("testFindForSpecialType5", function() {
        var text = "Dies März 2010 ist hoffentlich ein Datum";
        var searchItem = new SearchItem({});
        var erg =  searchItem.findSpecialType(text, ["date"], false, null);
        expect(erg[0].text).toBe("März 2010");
        expect(erg[0].getStart()).toBe(5);
        expect(erg[0].getEnd()).toBe(14);
        expect(erg[0].val.getTime()).toBe(new Date(2010,2,1).getTime());
    });
});


/*SearchItemTest.prototype.testDateFormat = function() {
    var rec = new Recognition();
    var date = new Date();
    date.setFullYear(2014, 4, 22);
    var dateString = rec.dateFormat(date, "dd.MM.YYYY");
    assertEquals("Datumstring ist nicht gleich!", "22.05.2014", dateString);
};*/


















