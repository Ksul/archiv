/**
 * Created by m500288 on 09.03.15.
 */


describe("Test für Recognition", function() {

    beforeEach(function () {
        REC.init();
    });

    it("testRecognize", function() {
        var iBox = companyhome.childByNamePath("/Archiv/Inbox");
        var doc = iBox.createNode("WebScriptTest", "my:archivContent");
        doc.properties.content.write(new Content("Zauberfrau Rechnung Nr 1001 Gesamtbetrag 200  Datum 14.02.2015"));
        var rules =
            '<documentTypes                                                                                                                                                                                                                                                                                 ' +
            'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"                                                                                                                                                                                                                                          ' +
            'xmlns:cm="http://www.alfresco.org/model/content/1.0"                                                                                                                                                                                                                                           ' +
            'xmlns:my="http://www.schulte.local/archiv"  xsi:noNamespaceSchemaLocation="doc.xsd" archivRoot="Archiv/" inBox="Inbox" mandatory="cm:title,my:documentDate,my:person" unknownBox="Unbekannt" errorBox="Fehler"  duplicateBox="Fehler/Doppelte" debugLevel="informational" maxDebugLength="40"> ' +
            '<archivTyp name="Zauberfrau" searchString="ZAUBERFRAU">                                                                                                                                                                                                                                        ' +
            '<archivZiel type="my:archivContent" />                                                                                                                                                                                                                                                         ' +
            '<archivPosition folder="Dokumente/Rechnungen/Rechnungen Zauberfrau/{tmp}">                                                                                                                                                                                                                     ' +
            '<archivZiel type="my:archivFolder" />                                                                                                                                                                                                                                                          ' +
            '</archivPosition>                                                                                                                                                                                                                                                                              ' +
            '<tags name="Rechnung" />                                                                                                                                                                                                                                                                       ' +
            '<tags name="Zauberfrau" />                                                                                                                                                                                                                                                                     ' +
            '<tags name="Steuerrelevant" />                                                                                                                                                                                                                                                                 ' +
            '<category name="Rechnung/Rechnung Zauberfrau" />                                                                                                                                                                                                                                               ' +
            '<searchItem name="person" fix="Klaus" target="my:person" />                                                                                                                                                                                                                                    ' +
            '<searchItem name="title" eval="new Date(new Date(\'{datum}\').setMonth(new Date(\'{datum}\').getMonth()-1))" target="cm:title">                                                                                                                                                                    ' +
            '<format formatString="MMMM YYYY" />                                                                                                                                                                                                                                                            ' +
            '</searchItem>                                                                                                                                                                                                                                                                                  ' +
            '<searchItem name="datum" text="Datum" objectTyp="date" target="my:documentDate">                                                                                                                                                                                                               ' +
            '<delimitter typ="start" text="&#0032;" count="1" />                                                                                                                                                                                                                                            ' +
            '<check lowerValue="01/01/2005" upperValue="01/01/2020" />                                                                                                                                                                                                                                      ' +
            '</searchItem>                                                                                                                                                                                                                                                                                  ' +
            '<searchItem name="betrag" text="Gesamtbetrag" word="1" objectTyp="float" target="my:amount">                                                                                                                                                                                                   ' +
            '<check lowerValue="0" upperValue="250" />                                                                                                                                                                                                                                                      ' +
            '<archivZiel aspect="my:amountable" />                                                                                                                                                                                                                                                          ' +
            '</searchItem>                                                                                                                                                                                                                                                                                  ' +
            '<searchItem name="id" text="Rechnung Nr" word="1" objectTyp="int" target="my:idvalue">                                                                                                                                                                                                         ' +
            '<check lowerValue="1000" upperValue="15000" />                                                                                                                                                                                                                                                 ' +
            '<archivZiel aspect="my:idable" />                                                                                                                                                                                                                                                              ' +
            '<format formatString="00000" />                                                                                                                                                                                                                                                                ' +
            '</searchItem>                                                                                                                                                                                                                                                                                  ' +
            '<searchItem name="tmp" objectTyp="date" value="datum">                                                                                                                                                                                                                                         ' +
            '<format formatString="YYYY" />                                                                                                                                                                                                                                                                 ' +
            '</searchItem>                                                                                                                                                                                                                                                                                  ' +
            '</archivTyp>                                                                                                                                                                                                                                                                                   ' +
            '</documentTypes>                                                                                                                                                                                                                                                                               ';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        REC.recognize(doc, new XMLObject(XMLDoc.docNode));
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).toBeNull();
        doc = companyhome.childByNamePath("/Archiv/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/WebScriptTest");
        expect(doc).not.toBeNull();
        expect(doc.isSubType("my:archivContent")).toBeTruthy();
        expect(doc.properties["my:amount"]).toBe(200);
        expect(doc.properties["my:documentDate"].getTime()).toBe(new Date(2015, 1, 14).getTime());
        expect(doc.properties["cm:title"]).toBe("Januar 2015");
        expect(doc.properties["my:person"]).toBe("Klaus");
        expect(doc.hasTag("Rechnung")).toBeTruthy();
        expect(doc.hasTag("Zauberfrau")).toBeTruthy();
        expect(doc.hasTag("Steuerrelevant")).toBeTruthy();
        expect(doc.hasAspect("my:amountable")).toBeTruthy();
        expect(doc.properties["cm:categories"][0].name).toBe("Rechnung Zauberfrau");
    });

    it("testUnknownDocument", function() {
        var iBox = companyhome.childByNamePath("Archiv/Inbox");
        var doc = iBox.createNode("WebScriptTest", "my:archivContent");
        doc.properties.content.write(new Content("Hansel Rechnung Nr 1001 Gesamtbetrag 200  Datum 14.02.2015"));
        var rules =
            '<documentTypes                                                                                                                                                                                                                                                                                 ' +
            'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"                                                                                                                                                                                                                                          ' +
            'xmlns:cm="http://www.alfresco.org/model/content/1.0"                                                                                                                                                                                                                                           ' +
            'xmlns:my="http://www.schulte.local/archiv"  xsi:noNamespaceSchemaLocation="doc.xsd" archivRoot="Archiv/" inBox="Inbox" mandatory="cm:title,my:documentDate,my:person" unknownBox="Unbekannt" errorBox="Fehler"  duplicateBox="Fehler/Doppelte" debugLevel="informational" maxDebugLength="40"> ' +
            '<archivTyp name="Zauberfrau" searchString="ZAUBERFRAU">                                                                                                                                                                                                                                        ' +
            '<archivZiel type="my:archivContent" />                                                                                                                                                                                                                                                         ' +
            '<archivPosition folder="Dokumente/Rechnungen/Rechnungen Zauberfrau/{tmp}">                                                                                                                                                                                                                     ' +
            '<archivZiel type="my:archivFolder" />                                                                                                                                                                                                                                                          ' +
            '</archivPosition>                                                                                                                                                                                                                                                                              ' +
            '</archivTyp>                                                                                                                                                                                                                                                                                   ' +
            '</documentTypes>                                                                                                                                                                                                                                                                               ';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        REC.recognize(doc, new XMLObject(XMLDoc.docNode));
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/WebScriptTest")).toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Unbekannt/WebScriptTest")).not.toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Fehler/WebScriptTest")).toBeNull();
    });

    it("Teste XML parsen", function () {
        var xmlString = "<documentTypes         " +
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "xmlns:cm=\"http://www.alfresco.org/model/content/1.0\" " +
            "xmlns:my=\"http://www.schulte.local/archiv\"  xsi:noNamespaceSchemaLocation=\"doc.xsd\" archivRoot=\"Archiv/\" inBox=\"Inbox\" mandatory=\"cm:title,my:documentDate,my:person\" unknownBox=\"Unbekannt\" errorBox=\"Fehler\"  duplicateBox=\"Fehler/Doppelte\" debugLevel=\"informational\" maxDebugLength=\"40\"> " +
            "   <!-- LVM Gehaltsabrechnung --> " +
            "<archivTyp name=\"LVMGehalt\" searchString=\"Verdienstabrechnung\">" +
            "    <archivZiel type=\"my:archivContent\" />" +
            "    <archivPosition folder=\"Dokumente/Gehalt/Gehalt LVM/{tmp}\">" +
            "    <archivZiel type=\"my:archivFolder\" />" +
            "    </archivPosition> "+
            "    <archivPosition link=\"true\" folder=\"Dokumente/LVM/Gehalt LVM/{tmp}\">" +
            "    <archivZiel type=\"my:archivFolder\" />" +
            "    </archivPosition>" +
            "    <tags name=\"Gehalt\" />" +
            "    <tags name=\"LVM\" />" +
            "    <category name=\"Gehalt/Gehalt Klaus\" />" +
            "    <searchItem name=\"person\" fix=\"Klaus\" target=\"my:person\" />" +
            "    <searchItem name=\"tmp\" objectTyp=\"date\" value=\"datum\">" +
            "    <format formatString=\"YYYY\" />" +
            "    </searchItem>" +
            "    <archivTyp name=\"Rückrechnung\" searchString=\"Rückrechnungsdifferenz\">" +
            "    <tags name=\"Rückrechnung\" />" +
            "    <searchItem name=\"titel\" text=\"Abrechnungsmonat\" word=\"2,2\"  />" +
            "    <searchItem name=\"title\" fix=\"Rückrechnung {titel}\" target=\"cm:title\"/>" +
            "    <searchItem name=\"datum\" text=\"Abrechnungsmonat\" word=\"2,2\" objectTyp=\"date\" target=\"my:documentDate\">" +
            "    <check lowerValue=\"01/01/2005\" upperValue=\"01/01/2020\" />" +
            "    </searchItem>" +
            "    <searchItem name=\"betrag\" text=\"Rückrechnungsdifferenz\" objectTyp=\"float\" target=\"my:amount\">" +
            "    <check lowerValue=\"-200\" upperValue=\"200\" /> " +
            "    <delimitter typ=\"start\" text=\"&#0032;\" count=\"1\" removeBlanks=\"after\" /> " +
            "    <archivZiel aspect=\"my:amountable\" /> " +
            "    </searchItem>" +
            "    </archivTyp> " +
            "    </documentTypes>";
        XMLDoc.loadXML(xmlString);
        XMLDoc.parse();
        var xml = new XMLObject(XMLDoc.docNode);
        expect(xml).not.toBeNull();
        expect(xml.archivRoot).toBe("Archiv/");
        expect(xml.debugLevel).toBe("informational");
        expect(xml.duplicateBox).toBe("Fehler/Doppelte");
        expect(xml.errorBox).toBe("Fehler");
        expect(xml.inBox).toBe("Inbox");
        expect(xml.mandatory).toBe("cm:title,my:documentDate,my:person");
        expect(xml.maxDebugLength).toBe("40");
        expect(xml.unknownBox).toBe("Unbekannt");
        expect(xml.archivTyp.constructor == Array).toBeTruthy();
        expect(xml.archivTyp.length).toBe(1);
        expect(xml.archivTyp[0].name).toBe("LVMGehalt");
        expect(xml.archivTyp[0].searchString).toBe("Verdienstabrechnung");
        expect(xml.archivTyp[0].archivPosition.constructor == Array).toBeTruthy();
        expect(xml.archivTyp[0].archivPosition.length).toBe(2);
        expect(xml.archivTyp[0].archivTyp.constructor == Array).toBeTruthy();
        expect(xml.archivTyp[0].archivTyp.length).toBe(1);
        expect(xml.archivTyp[0].archivZiel.constructor == Array).toBeTruthy();
        expect(xml.archivTyp[0].archivZiel.length).toBe(1);
        expect(xml.archivTyp[0].category.constructor == Array).toBeTruthy();
        expect(xml.archivTyp[0].category.length).toBe(1);
        expect(xml.archivTyp[0].searchItem.constructor == Array).toBeTruthy();
        expect(xml.archivTyp[0].searchItem.length).toBe(2);
        expect(xml.archivTyp[0].tags.constructor == Array).toBeTruthy();
        expect(xml.archivTyp[0].tags.length).toBe(2);
        expect(xml.archivTyp[0].archivPosition[0].folder).toBe("Dokumente/Gehalt/Gehalt LVM/{tmp}");
        expect(xml.archivTyp[0].archivPosition[1].folder).toBe("Dokumente/LVM/Gehalt LVM/{tmp}");
        expect(xml.archivTyp[0].archivPosition[0].archivZiel.constructor == Array).toBeTruthy();
        expect(xml.archivTyp[0].archivPosition[1].archivZiel.constructor == Array).toBeTruthy();
        expect(xml.archivTyp[0].archivPosition[0].archivZiel.length).toBe(1);
        expect(xml.archivTyp[0].archivPosition[1].archivZiel.length).toBe(1);
        expect(xml.archivTyp[0].archivPosition[0].archivZiel[0].type).toBe("my:archivFolder");
        expect(xml.archivTyp[0].archivPosition[1].archivZiel[0].type).toBe("my:archivFolder");
        expect(xml.archivTyp[0].archivTyp[0].name).toBe("Rückrechnung");
        expect(xml.archivTyp[0].archivTyp[0].searchString).toBe("Rückrechnungsdifferenz");
        expect(xml.archivTyp[0].archivTyp[0].searchItem.constructor == Array).toBeTruthy();
        expect(xml.archivTyp[0].archivTyp[0].tags.constructor == Array).toBeTruthy();
        expect(xml.archivTyp[0].archivTyp[0].searchItem.length).toBe(4);
        expect(xml.archivTyp[0].archivTyp[0].tags.length).toBe(1);
        expect(xml.archivTyp[0].archivTyp[0].searchItem[0].name).toBe("titel");
        expect(xml.archivTyp[0].archivTyp[0].searchItem[0].text).toBe("Abrechnungsmonat");
        expect(xml.archivTyp[0].archivTyp[0].searchItem[0].word).toBe("2,2");
        expect(xml.archivTyp[0].archivTyp[0].searchItem[1].fix).toBe("Rückrechnung {titel}");
        expect(xml.archivTyp[0].archivTyp[0].searchItem[1].name).toBe("title");
        expect(xml.archivTyp[0].archivTyp[0].searchItem[1].target).toBe("cm:title");
        expect(xml.archivTyp[0].archivTyp[0].searchItem[2].objectTyp).toBe("date");
        expect(xml.archivTyp[0].archivTyp[0].searchItem[2].name).toBe("datum");
        expect(xml.archivTyp[0].archivTyp[0].searchItem[2].target).toBe("my:documentDate");
        expect(xml.archivTyp[0].archivTyp[0].searchItem[2].word).toBe("2,2");
        expect(xml.archivTyp[0].archivTyp[0].searchItem[2].text).toBe("Abrechnungsmonat");
        expect(xml.archivTyp[0].archivTyp[0].searchItem[2].check.constructor == Array).toBeTruthy();
        expect(xml.archivTyp[0].archivTyp[0].searchItem[2].check.length).toBe(1);
        expect(xml.archivTyp[0].archivTyp[0].searchItem[2].check[0].lowerValue).toBe("01/01/2005");
        expect(xml.archivTyp[0].archivTyp[0].searchItem[2].check[0].upperValue).toBe("01/01/2020");
        expect(xml.archivTyp[0].archivTyp[0].searchItem[3].name).toBe("betrag");
        expect(xml.archivTyp[0].archivTyp[0].searchItem[3].objectTyp).toBe("float");
        expect(xml.archivTyp[0].archivTyp[0].searchItem[3].target).toBe("my:amount");
        expect(xml.archivTyp[0].archivTyp[0].searchItem[3].text).toBe("Rückrechnungsdifferenz");
        expect(xml.archivTyp[0].archivTyp[0].searchItem[3].check.constructor == Array).toBeTruthy();
        expect(xml.archivTyp[0].archivTyp[0].searchItem[3].check.length).toBe(1);
        expect(xml.archivTyp[0].archivTyp[0].searchItem[3].archivZiel.constructor == Array).toBeTruthy();
        expect(xml.archivTyp[0].archivTyp[0].searchItem[3].archivZiel.length).toBe(1);
        expect(xml.archivTyp[0].archivTyp[0].searchItem[3].delimitter.constructor == Array).toBeTruthy();
        expect(xml.archivTyp[0].archivTyp[0].searchItem[3].delimitter.length).toBe(1);
        expect(xml.archivTyp[0].archivTyp[0].searchItem[3].archivZiel[0].aspect).toBe("my:amountable");
        expect(xml.archivTyp[0].archivTyp[0].searchItem[3].check[0].lowerValue).toBe("-200");
        expect(xml.archivTyp[0].archivTyp[0].searchItem[3].check[0].upperValue).toBe("200");
        expect(xml.archivTyp[0].archivTyp[0].searchItem[3].delimitter[0].count).toBe("1");
        expect(xml.archivTyp[0].archivTyp[0].searchItem[3].delimitter[0].removeBlanks).toBe("after");
        expect(xml.archivTyp[0].archivTyp[0].searchItem[3].delimitter[0].text).toBe("&#0032;");
        expect(xml.archivTyp[0].archivTyp[0].searchItem[3].delimitter[0].typ).toBe("start");
        expect(xml.archivTyp[0].archivZiel[0].type).toBe("my:archivContent");
        expect(xml.archivTyp[0].category[0].name).toBe("Gehalt/Gehalt Klaus");
        expect(xml.archivTyp[0].searchItem[0].name).toBe("person");
        expect(xml.archivTyp[0].searchItem[0].fix).toBe("Klaus");
        expect(xml.archivTyp[0].searchItem[0].target).toBe("my:person");
        expect(xml.archivTyp[0].searchItem[1].name).toBe("tmp");
        expect(xml.archivTyp[0].searchItem[1].objectTyp).toBe("date");
        expect(xml.archivTyp[0].searchItem[1].value).toBe("datum");
        expect(xml.archivTyp[0].searchItem[1].format.constructor == Array).toBeTruthy();
        expect(xml.archivTyp[0].searchItem[1].format.length).toBe(1);
        expect(xml.archivTyp[0].searchItem[1].format[0].formatString).toBe("YYYY");
        expect(xml.archivTyp[0].tags[0].name).toBe("Gehalt");
        expect(xml.archivTyp[0].tags[1].name).toBe("LVM");
    });
});




