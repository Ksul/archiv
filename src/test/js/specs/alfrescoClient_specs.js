
describe("Tests für alfrescoClient.js", function() {
    //
    // beforeEach(function(){
    //
    //     jasmine.Ajax.install();
    //     jasmine.Ajax.stubRequest('http://localhost:8080/Archiv/listFolderWithPagination').andReturn({
    //         responseText: 'YOUR_RAW_STUBBED_DATA_HERE'
    //     });
    //
    // });

    beforeEach(function () {
        // var jsdom = require("jsdom").jsdom;
        // global.window = jsdom().parentWindow;
        // global.jQuery = global.$ = require("jquery")(window);
 //       }
    });

    
    afterEach(function () {

        //jasmine.Ajax.uninstall();


        var json = executeService({"name": "getNodeId",
                                    "url": "http://localhost:8080/Archiv",
                                    "ignoreError": true}, [
                                         {"name": "filePath",
                                          "value": "/Archiv/Dokumente/TestFolder"}
                                    ]);

        if (json.success) {

            json = executeService({
                "name": "deleteFolder",
                "ignoreError": true,
                "errorMessage": "Ordner konnte nicht gelöscht werden!"
            }, [
                {"name": "documentId", "value": json.data.objectId}
            ]);
        }

    });


    it("testLoadTable", function() {

        archivFolderId = 8;
        documentFolderId = 9;

        alfrescoViewModeMenu = null;
        alfrescoDocumentSelectMenu=null;


        $('body').html($('<div id="dtable2">\n' +
            '  <table id="alfrescoTabelle"></table>\n' +
            '</div>\n' +
            '<div>\n' +
            '<ul id="selectionMenuAlfrescoDocuments" class="sf-menu" style="height: 15px;"></ul>\n' +
            '<ul id="actionMenuAlfrescoDocuments" class="sf-menu" style="height: 15px;"></ul>\n' +
            '<ul id="viewMenuAlfresco" class="sf-menu" style="height: 15px;"></ul>\n' +
            '</div>\n' +
            '<div class="header ui-widget-header dataTableFooter" id="alfrescoTableFooter"></div>\n' +
            '</div>'));
        createAlfrescoMenus();

        loadAlfrescoTable();
        expect(alfrescoTabelle).not.toBe(null);
    });

    it("testLoadFolderTable", function() {

        archivFolderId = 0;
        alfrescoFolderActionMenu = null;
        alfrescoFolderSelectMenu = null;


        $('body').html($(' <div id="dtable3">\n' +
            '  <table id="alfrescoFolderTabelle"></table>\n' +
            '</div>\n' +
            '<div>\n' +
            '<ul id="selectionMenuAlfrescoFolder" class="sf-menu" style="height: 15px;"></ul>\n' +
            '<ul id="actionMenuAlfrescoFolder" class="sf-menu" style="height: 15px;"></ul>\n' +
            '</div>\n' +
            '<div class="header ui-widget-header dataTableFooter" id="alfrescoFolderTableFooter"></div>\n' +
            '</div>'));
        createAlfrescoFolderMenus();
        loadAlfrescoFolderTable();
        expect(alfrescoFolderTabelle).not.toBe(null);
    });

    it("testLoadSearchTable", function() {

        searchViewModeMenu = null;
        searchDocumentSelectMenu = null;
        alfrescoSearchDocumentActionMenu = null;

        $('body').html($(' <div id="dtable4">\n' +
            '  <table id="alfrescoSearchTabelle"></table>\n' +
            '</div>\n' +
            '<div>\n' +
            '<ul id="selectionMenuAlfrescoSearchDocuments" class="sf-menu" style="height: 15px;"></ul>\n' +
            '<ul id="actionMenuAlfrescoSearchDocuments" class="sf-menu" style="height: 15px;"></ul>\n' +
            '<ul id="viewMenuSearch" class="sf-menu" style="height: 15px;"></ul>\n' +
            '</div>\n' +
            '<div class="header ui-widget-header dataTableFooter" id="alfrescoSearchTableFooter"></div>\n' +
            '</div>'));
        createSearchMenus();
        loadAlfrescoSearchTable();
        expect(alfrescoSearchTabelle).not.toBe(null);
    });

    it("testLoadTree", function() {

        checkAndBuidAlfrescoEnvironment();

        $('body').html($(' <div id="dtable2">\n' +
            '  <table id="alfrescoTabelle"></table>\n' +
            '</div>\n' +
            '<div class="header ui-widget-header dataTableFooter" id="alfrescoTableFooter"></div>\n' +
            '</div>\n' +
            ' <div id="dtable3">\n' +
            '  <table id="alfrescoFolderTabelle"></table>\n' +
            '</div>\n' +
            '<div class="header ui-widget-header dataTableFooter" id="alfrescoFolderTableFooter"></div>\n' +
            '</div>\n'+
            '<div id="tree"></div>'));
        loadAlfrescoTree();
        expect(tree).not.toBe(null);

    });

    it("testAimNode", function() {

        alfrescoViewModeMenu = null;
        alfrescoDocumentSelectMenu=null;

        createAlfrescoMenus();
        checkAndBuidAlfrescoEnvironment();


        var extraProperties = {
            "cmis:folder": {"cmis:name": "TestFolder"},
            "P:cm:titled": {"cm:title": "TestFolder", "cm:description": "Ordner für Tests"}
        };
        json = executeService({"name": "createFolder"}, [
            {"name": "documentId", "value": documentFolderId},
            {"name": "extraProperties", "value": extraProperties}
        ]);

        expect(json.success).toBe(true);
        expect(json.data).not.toBe(null);
        expect(json.data.objectId).not.toBe(null);

        var folderId = json.data.objectID;

        json =  executeService({
            "url": "http://localhost:8080/Archiv",
            "name": "createDocument",
            "ignoreError": true
        }, [
            {"name": "documentId", "value": folderId},
            {"name": "fileName", "value": "Test"},
            {"name": "content", "value": ""},
            {"name": "mimeType", "value": "text/plain"},
            {
                "name": "extraProperties",
                "value": {"cmis:document":{"cmis:name": "Test"}, "P:cm:titled":{"cm:description":"Test"}}
            },
            {"name": "versionState", "value": "major"}

        ]);

        expect(json.success).toBe(true);
        expect(json.data).not.toBe(null);
        expect(json.data.objectId).not.toBe(null);

        $('body').html($(' <div id="dtable2">\n' +
            '  <table id="alfrescoTabelle"></table>\n' +
            '</div>\n' +
            '<div>\n' +
            '<ul id="selectionMenuAlfrescoDocuments" class="sf-menu" style="height: 15px;"></ul>\n' +
            '<ul id="actionMenuAlfrescoDocuments" class="sf-menu" style="height: 15px;"></ul>\n' +
            '<ul id="viewMenuAlfresco" class="sf-menu" style="height: 15px;"></ul>\n' +
            '</div>\n' +
            '<div class="header ui-widget-header dataTableFooter" id="alfrescoTableFooter"></div>\n' +
            '</div>\n' +
            ' <div id="dtable3">\n' +
            '  <table id="alfrescoFolderTabelle"></table>\n' +
            '</div>\n' +
            '<div>\n' +
            '<ul id="selectionMenuAlfrescoFolder" class="sf-menu" style="height: 15px;"></ul>\n' +
            '<ul id="actionMenuAlfrescoFolder" class="sf-menu" style="height: 15px;"></ul>\n' +
            '</div>\n' +
            '<div class="header ui-widget-header dataTableFooter" id="alfrescoFolderTableFooter"></div>\n' +
            '</div>\n' +
            '<div id="tree"></div>'));
        loadAlfrescoTree();
        expect(tree).not.toBe(null);

        loadAlfrescoTable();


        var erg = aimNode(json.data);
        expect(erg).toBe(true);
        expect(tree.jstree('get_selected').length > 0).toBeTruthy();
        expect(tree.jstree('get_selected')[0]).toBe(folderId);
    });


});