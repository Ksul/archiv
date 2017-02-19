/**
 * Created by m500288 on 13.05.14.
 */
/*
VerteilungTest = TestCase("VerteilungTest");

VerteilungTest.prototype.testCheckServerStatus = function() {
    //var verteilung = new myArchiv.Verteilung();
    assertEquals("Hello World.", checkServerStatus("World"));
};


VerteilungTest.prototype.testURL = function() {
    var exp = "(http|https)://[\w\-_]+(\.[\w\-_]+)+([\w\-\.,@?^=%&amp;:/~\+#]*[\w\-\@?^=%&amp;/~\+#])?";
    var reg = new RegExp(exp);
    var result = reg.test("http:\/\/localhost:8080\/alfresco\/");
    assertTrue(result);

};*/

describe("Tests für util.js", function() {


    it("Teste Datum parsen", function () {
        var dateString = "Thu May 22 16:23:07 CEST 2014";
        var date = parseDate(dateString);
        var vergleich = new Date(2014, 4, 22, 16, 23, 7, 0);
        expect(vergleich.getTime()).toBe(date.getTime());
    });
});

