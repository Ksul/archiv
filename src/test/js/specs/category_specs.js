/**
 * Created by m500288 on 20.02.15.
 */

describe("Test für Kategorie", function() {

    beforeEach(function () {
        REC.init();
        companyhome.init();
        classification.init();
    });

    it("test1", function() {
        var rules = '<category name="Steuern/Einkommen" debugLevel="trace" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var category = new Category(new XMLObject(XMLDoc.docNode));
        category.resolve(REC.currentDocument);
        expect( classification.rootCategories.contains((new BasicObject("Steuern")))).toBeTruthy();
        var cat = classification.rootCategories.get(new BasicObject("Steuern"));
        expect(cat.subCategories.contains(new BasicObject("Einkommen"))).toBeTruthy();
    });


    it("test2", function() {
        classification.createRootCategory("cm:generalclassifiable", "Steuern");
        var rules = '<category name="Steuern/Einkommen" debugLevel="trace" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var category = new Category(new XMLObject(XMLDoc.docNode));
        category.resolve(REC.currentDocument);
        expect( classification.rootCategories.contains((new BasicObject("Steuern")))).toBeTruthy();
        var cat = classification.rootCategories.get(new BasicObject("Steuern"));
        expect(cat.subCategories.contains(new BasicObject("Einkommen"))).toBeTruthy();
    });
    
});



