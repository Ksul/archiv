
/**
 * startet die normalen Alfresco View
 */
function showAlfrescoNormalView(){
    alfrescoTabelle.settings().init().iconView = false;
    viewMenuNormal.get(0).children[0].children[0].setAttribute('class','fa fa-file-text-o fa-1x');
    viewMenuNormal.children('li:first').superfish('hide');
    alfrescoTabelle.column(0).visible(true);
    alfrescoTabelle.column(1).visible(true);
    alfrescoTabelle.column(2).visible(false);
    //alfrescoLayout.children.center.alfrescoCenterInnerLayout.children.center.alfrescoCenterCenterInnerLayout.show("north");
    resizeTable("alfrescoCenterCenterCenter", "dtable2", "alfrescoTabelle", "alfrescoTabelleHeader", "alfrescoTableFooter");
}

/**
 * startet die Alfresco Icon View
 */
function showAlfrescoIconView(){
    alfrescoTabelle.settings().init().iconView = true;
    viewMenuNormal.get(0).children[0].children[0].setAttribute('class','fa fa-photo fa-1x');
    viewMenuNormal.children('li:first').superfish('hide');
    alfrescoTabelle.column(0).visible(false);
    alfrescoTabelle.column(1).visible(false);
    alfrescoTabelle.column(2).visible(true);
    //alfrescoLayout.children.center.alfrescoCenterInnerLayout.children.center.alfrescoCenterCenterInnerLayout.hide("north");

    resizeTable("alfrescoCenterCenterCenter", "dtable2", "alfrescoTabelle", "alfrescoTabelleHeader", "alfrescoTableFooter");
}


/**
 * startet die normalen Alfresco SearchView
 */
function showAlfrescoSearchNormalView(){
    alfrescoSearchTabelle.settings().init().iconView = false;
    viewMenuSearch.get(0).children[0].children[0].setAttribute('class','fa fa-file-text-o fa-1x');
    viewMenuSearch.children('li:first').superfish('hide');
    alfrescoSearchTabelle.column(0).visible(true);
    alfrescoSearchTabelle.column(1).visible(true);
    alfrescoSearchTabelle.column(2).visible(false);
    resizeTable("searchCenter", "dtable4", "alfrescoSearchTabelle", "alfrescoSearchTabelleHeader", "alfrescoSearchTableFooter");
}

/**
 * startet die Alfresco Search Icon View
 */
function showAlfrescoSearchIconView() {
    alfrescoSearchTabelle.settings().init().iconView = true;
    viewMenuSearch.get(0).children[0].children[0].setAttribute('class','fa fa-photo fa-1x');
    viewMenuSearch.children('li:first').superfish('hide');
    alfrescoSearchTabelle.column(0).visible(false);
    alfrescoSearchTabelle.column(1).visible(false);
    alfrescoSearchTabelle.column(2).visible(true);
    resizeTable("searchCenter", "dtable4", "alfrescoSearchTabelle", "alfrescoSearchTabelleHeader", "alfrescoSearchTableFooter");
}

/**
 * Eventhandler der für die Verarbeitung von fallen gelassen Dateien auf die Inbox zuständig ist
 * @param evt  das Event
 */
function handleDropInbox(evt) {
    evt.stopPropagation();
    evt.preventDefault();
    var files = evt.dataTransfer.files;
    for ( var i = 0; i < files.length; i++) {
        var f = files[i];
        if (f) {
            var reader = new FileReader();
            reader.onloadend = (function (f) {
                return function (evt) {
                    if (evt.target.readyState == FileReader.DONE) {
                        var content = evt.target.result;
                        var json = executeService({"name": "createDocument", "errorMessage": "Dokument konnten nicht im Alfresco angelegt werden!"},[
                            {"name": "documentId", "value":inboxFolderId},
                            {"name": "fileName", "value": f.name},
                            {"name": "content", "value": base64EncArr(strToUTF8Arr(content))},
                            {"name": "mimeType", "value": "application/pdf"},
                            {"name": "extraProperties", "value": {}},
                            {"name": "versionState", "value": "major"}

                        ]);
                    }

                }  })(f);
            blob = f.slice(0, f.size + 1);
            reader.readAsBinaryString(blob);
        } else {
            message("Fehler", "Failed to load file!");
        }
    }
}

/**
 * berechnet die passende Pagelength der Tabelle
 * @param panel         das Layoutpanel, welches die Tabelle enthält
 * @param divId         die Id des DIV's welches die Tabelle enthält
 * @param tabelle     die Id der Tabelle
 * @param headerId      die Id des headers
 * @param footerId      die Id des Footers
 */
function calculateTableHeight(panel, divId, tabelle, headerId, footerId) {
    var div, table, rowHeight = 0, availableHeight = 0, topPanel = 0, downPanel = 0,columnPanel = 0, headerPanel = 0, footerPanel = 0;
    var children;
    div = $('#'+divId);
    availableHeight = $('#'+panel).height();
    children =  div.children().children();
    if (children.length > 0)
        topPanel = children[0].offsetHeight;
    if (children.length > 2)
        downPanel = children[2].offsetHeight;
    if (children.length > 1)
        columnPanel = children[0].offsetHeight;
    headerPanel = $('#'+headerId).height();
    footerPanel = $('#'+footerId).height();
    if (exist(tabelle) && exist(tabelle.settings().init().iconView) && tabelle.settings().init().iconView) // fest nach aktueller CSS
        rowHeight = 121;
    else
        rowHeight = 33.5;

    availableHeight -= topPanel;
    availableHeight -= headerPanel;
    availableHeight -= columnPanel;
    availableHeight -= downPanel;
    availableHeight -= footerPanel;

    return Math.floor(availableHeight / rowHeight);
}

/**
 * setzt die Pagelength in der Tabelle
 * @param panel         das Layoutpanel, welches die Tabelle enthält
 * @param divId         die Id des DIV's welches die Tabelle enthält
 * @param tabelleId     die Id der Tabelle
 * @param headerId      die Id des headers
 * @param footerId      die Id des Footers
 */
function resizeTable(panel, divId, tabelleId, headerId, footerId) {

    var tabelle = $('#'+tabelleId).DataTable();
    var drawRows = calculateTableHeight(panel, divId, tabelle, headerId, footerId);

    if ( drawRows !== Infinity && drawRows !== -Infinity &&
        ! isNaN( drawRows )   && drawRows > 0 &&
        drawRows !== tabelle.page.len()
    ) {
        tabelle.page.len( drawRows ).draw("page");
    }
}


function asumeCountOfTableEntries(panel,  divId, tabelleId,headerId, footerId) {
    var div = $('#'+divId);
    var completePanel = $('#' + panel).height();
    var topPanel = div.children().children()[0].offsetHeight;
    var downPanel = div.children().children()[2].offsetHeight;
    var columnPanel = div.children().children()[1].children[0].offsetHeight;
    var headerPanel = $('#' + headerId).height();
    var footerPanel = $('#' + footerId).height();
    var rowHeight = $('.odd') + 2;
    return Math.floor((completePanel - topPanel - headerPanel - columnPanel - downPanel - footerPanel) / rowHeight);
}

function toggleLiveResizing() {
    $.each($.layout.config.borderPanes, function (i, pane) {
        var o = verteilungLayout.options[pane];
        o.livePaneResizing = !o.livePaneResizing;
    });
}


function toggleStateManagement(skipAlert, mode) {
    if (!$.layout.plugins.stateManagement) return;

    var options = verteilungLayout.options.stateManagement
        , enabled = options.enabled // current setting
        ;
    if ($.type(mode) === "boolean") {
        if (enabled === mode) return; // already correct
        enabled = options.enabled = mode
    }
    else
        enabled = options.enabled = !enabled; // toggle option

    if (!enabled) { // if disabling state management...
        verteilungLayout.deleteCookie(); // ...clear cookie so will NOT be found on next refresh
        if (!skipAlert)
            alert('This layout will reload as the options specify \nwhen the page is refreshed.');
    }
    else if (!skipAlert)
        alert("This layout will save and restore its last state \nwhen the page is refreshed.");
}

/**
 * baut das Layout der Anwendung auf
 */
function loadLayout() {
    try {
        var clientPageLayoutSettings = {
            name: "clientLayoutSettings",
            center__paneSelector: "#clientPage",
            resizable: false
        };
        // Seitenlayout
        var pageLayoutSettings = {
            name: "pageLayout",
            fxName: "slide",		// none, slide, drop, scale
            fxSpeed_open: 800,
            fxSpeed_close: 1000,
            fxSettings_open: {easing: "easeInQuint"},
            fxSettings_close: {easing: "easeOutQuint"},
            resizerClass: "ui-widget-content",
            togglerClass: "ui-widget-content",
            spacing_open: 8,
            spacing_closed: 12,
            closable: true,
            resizable: false,
            center: {
                paneSelector: "#tabs",
                resizable: true,
                slidable: true,
                size: "auto"                                         
            },
            south: {
                paneSelector: "#contentSouth",
                contentSelector: ".ui-widget-content",
                size: 0.1,
                resizable: true,
                slidable: true,
                resizeWithWindow: true,
                onresize: function () {
                    if (REC.exist(Verteilung.outputEditor))
                        Verteilung.outputEditor.resize();
                }
            }
        };

        var contentLayoutSettings = {
            name: "contentLayout",
            spacing_open: 0,
            spacing_closed: 12,
            resizable: false,
            closable: false,
            initPanes: true,
            showDebugMessages: true,
            contentSelector: ".ui-widget-content",
            north: {
                paneSelector: "#tabNorth",
                children: {
                    name: "tabNorthInnerLayout",
                    center: {
                        size: 15,
                        resizable: false,
                        closable: false,
                        name: "tabNorthInnerCenterLayout",
                        paneSelector: "#tabNorthInnerCenter"
                    }
                }
            },
            center: {
                paneSelector: "#tabPanels"
            },
            activate: $.layout.callbacks.resizeTabLayout
        };

        // SearchTab
        var searchLayoutSettings = {
            name: "searchLayout",
            size: "auto",
            minSize: 13,
            initClosed: false,
            initHidden: false,
            resizerTip: "Resize This Pane",
            fxName: "slide",
            fxSpeed_open: 800,
            fxSpeed_close: 1000,
            fxSettings_open: {easing: "easeInQuint"},
            fxSettings_close: {easing: "easeOutQuint"},
            closable: true,
            resizable: true,
            resizerClass: "ui-widget-content",
            togglerClass: "ui-widget-content",
            //slidable:				true,
            livePaneResizing: true,
            spacing_open: 8,
            spacing_closed: 12,
            initPanes: true,
            resizeWithWindow: false,
            contentSelector: ".ui-widget-content",
            north: {
                paneSelector: "#searchNorth",
                name: "searchNorthLayout",
                minSize: 34,
                maxSize: 34,
                resizable: false,
                closable: false,
                slidable: false,
                children: {
                    name: "searchNorthInnerLayout",
                    center: {
                        size: "auto",
                        name: "searchNorthCenterLayout",
                        paneSelector: "#searchNorthCenter"
                    },
                    east: {
                        size: 90,
                        name: "searchNorthEastLayout",
                        paneSelector: "#searchNorthEast"
                    }
                }
            },
            center: {
                        paneSelector: "#searchCenter",
                        name: "searchCenterLayout",
                        minHeight: 80,
                        size: .6,
                        resizable: true,
                        slidable: true,
                onresize: function () {
                    try {
                        resizeTable("searchCenter", "dtable4", "alfrescoSearchTabelle", "alfrescoSearchTabelleHeader", "alfrescoSearchTableFooter");
                    } catch (e) {
                        errorHandler(e);
                    }
                }
            }
        };
        //AlfrescoTab
        var alfrescoLayoutSettings = {
            name: "alfrescoLayout",
            size: "auto",
            minSize: 13,
            initClosed: false,
            initHidden: false,
            resizerTip: "Resize This Pane",
            fxName: "slide",
            fxSpeed_open: 800,
            fxSpeed_close: 1000,
            fxSettings_open: {easing: "easeInQuint"},
            fxSettings_close: {easing: "easeOutQuint"},
            closable: true,
            resizable: true,
            resizerClass: "ui-widget-content",
            togglerClass: "ui-widget-content",
            //slidable:				true,
            livePaneResizing: true,
            spacing_open: 8,
            spacing_closed: 12,
            initPanes: true,
            resizeWithWindow: false,
            contentSelector: ".ui-widget-content",
            north: {
                paneSelector: "#alfrescoNorth",
                name: "alfrescoNorthLayout",
                minSize: 34,
                maxSize: 34,
                resizable: false,
                closable: false,
                children: {
                    name: "alfrescoNorthInnerLayout",
                    center: {
                        name: "alfrescoNorthCenterLayout",
                        paneSelector: "#alfrescoNorthCenter"
                    },
                    east: {
                        size: 90,
                        name: "alfrescoNorthEastLayout",
                        paneSelector: "#alfrescoNorthEast"
                    }
                }
            },
            west: {
                paneSelector: "#alfrescoWest",
                name: "alfrescoWestLayout",
                size: .2,
                fxSettings_open: {easing: "easeOutBounce"},
                closable: true,
                resizable: true,
                slidable: true
            },
            center: {
                paneSelector: "#alfrescoCenter",
                name: "alfrescoCenterLayout",
                minHeight: 80,
                size: .8,
                children: {
                    name: "alfrescoCenterInnerLayout",
                    spacing_open: 8,
                    spacing_closed: 12,
                    center: {
                        size: "auto",

                        name: "alfrescoCenterCenterLayout",
                        paneSelector: "#alfrescoCenterCenter",

                        children: {
                            resizable: true,
                            closable: true,
                            slidable: true,
                            resizerTip: "Größe ändern",
                            fxName: "slide",
                            fxSpeed_open: 800,
                            fxSpeed_close: 1000,
                            fxSettings_open: {easing: "easeInQuint"},
                            fxSettings_close: {easing: "easeOutQuint"},
                            resizerClass: "ui-widget-content",
                            togglerClass: "ui-widget-content",
                            livePaneResizing: true,
                            spacing_open: 8,
                            spacing_closed: 12,
                            name: "alfrescoCenterCenterInnerLayout",
                            north: {
                                size: 225,
                                paneSelector: "#alfrescoCenterCenterNorth",
                                resizeable: true,
                                name: "alfrescoCenterCenterNorthLayout",
                                onresize: function () {
                                    try {
                                        resizeTable("alfrescoCenterCenterNorth", "dtable3", "alfrescoFolderTabelle", "alfrescoFolderTabelleHeader", "alfrescoFolderTableFooter");
                                        resizeTable("alfrescoCenterCenterCenter", "dtable2", "alfrescoTabelle", "alfrescoTabelleHeader", "alfrescoTableFooter");
                                    } catch (e) {
                                        errorHandler(e);
                                    }
                                }
                            },
                            center: {
                                size: "auto",
                                resizeable: true,
                                paneSelector: "#alfrescoCenterCenterCenter",
                                name: "alfrescoCenterCenterCenterLayout",
                                onresize: function () {
                                    try {
                                        resizeTable("alfrescoCenterCenterCenter", "dtable2", "alfrescoTabelle", "alfrescoTabelleHeader", "alfrescoTableFooter");
                                      } catch (e) {
                                        errorHandler(e);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
        //VerteilungTab
        var verteilungLayoutSettings = {
            name: "verteilungLayout",
            size: "auto",
            minSize: 13,
            initClosed: false,
            resizerTip: "Resize This Pane",
            fxName: "slide",
            fxSpeed_open: 800,
            fxSpeed_close: 1000,
            fxSettings_open: {easing: "easeInQuint"},
            fxSettings_close: {easing: "easeOutQuint"},
            closable: true,
            resizable: true,
            resizerClass: "ui-widget-content",
            togglerClass: "ui-widget-content",
            //slidable:				true,
            livePaneResizing: true,
            spacing_open: 8,
            spacing_closed: 12,
            initPanes: true,
            resizeWithWindow: false,
            contentSelector: ".ui-widget-content",
            west: {
                paneSelector: "#verteilungWest",
                size: .4,
                fxSettings_open: {
                    easing: "easeOutBounce"
                },
                closable: true,
                resizable: true,
                slidable: true,
                onresize: function () {
                    try {
                        Verteilung.textEditor.resize();
                        resizeTable("verteilungWest", "dtable", "tabelle", "verteilungTabelleHeader", "verteilungTableFooter");
                    } catch (e){
                        errorHandler(e);
                    }
                }
            },
            center: {
                paneSelector: "#verteilungCenter",
                initHidden: false,
                minHeight: 80,
                size: .45,
                initClosed: false,
                onresize: function () {
                    Verteilung.rulesEditor.resize();
                }
            },
            east: {
                size:.15,
                paneSelector: "#verteilungEast",
                onresize: function () {
                    Verteilung.propsEditor.resize();
                }
            },
            //	enable state management
            stateManagement__enabled: false,
            showDebugMessages: true
        };


        // create the tabs before the page layout because tabs will change the height of the north-pane
        tabLayout = $("#tabs").tabs({
            // using callback addon
            activate: $.layout.callbacks.resizeTabLayout,
            beforeActivate: function (event, ui) {
                if (ui.newPanel.attr('id') == "tab2")
                    $('#alfrescoSearch').focus().select();
            },
            active : 1

            /* OR with a custom callback
             activate: function (evt, ui) {
             $.layout.callbacks.resizeTabLayout( evt, ui );
             // other code...
             }
             */
        });

        globalLayout = $('body').layout(clientPageLayoutSettings);

        $('#clientPage').layout(pageLayoutSettings);
        $('#tabs').layout(contentLayoutSettings);
        verteilungLayout = $('#tab3').layout(verteilungLayoutSettings);
        searchLayout = $('#tab2').layout(searchLayoutSettings);
        alfrescoLayout = $('#tab1').layout(alfrescoLayoutSettings);

        globalLayout.deleteCookie();
        globalLayout.options.stateManagement.autoSave = false;
        // if there is no state-cookie, then DISABLE state management initially
        var cookieExists = !$.isEmptyObject(verteilungLayout.readCookie());
        if (!cookieExists) toggleStateManagement(true, false);
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * baut die Alfresco Tabelle auf.
 */
function loadAlfrescoTable() {

    /**
     * zeigt das Dokument in einem neuen Tab an
     * @param obj     das Objekt
     * @param event   das Event
     */
    function openDocument(obj, event) {
        try {
            event.preventDefault();
            event.stopImmediatePropagation();
            var erg = executeService({"name": "openDocument", "errorMessage": "Dokument konnten nicht geöffnet werden!"}, [
                {"name": "documentId", "value": alfrescoTabelle.row($(obj).closest('tr')).data().objectID}
            ]);
            if (erg.success) {
                var  file = new Blob([base64DecToArr(erg.data)], { type: erg.mimeType });
                var fileURL = URL.createObjectURL(file);
                window.open(fileURL);
            }

         } catch (e) {
            errorHandler(e);
        }
    }

    try {
        var duration;
        $.fn.dataTable.moment('DD.MM.YY');
        alfrescoTabelle = $('#alfrescoTabelle').DataTable({
            "jQueryUI": false,
            "pagingType": "paging_with_jqui_icons",
            "data": [],
            "scrollX": "100%",
            "scrollXInner": "100%",
            "autoWidth": false,
            "deferRender": true,
            "lengthChange": false,
            "searching": false,
            "order": [[3, 'desc']],
            "processing": true,
            "serverSide": true,
            "iconView": false,
            "folderId" : archivFolderId,
            "withFolder": 1,
            "itemsToSkip": 0,
            "pageLength": calculateTableHeight("alfrescoCenterCenterCenter", "dtable2", alfrescoTabelle, "alfrescoTabelleHeader", "alfrescoTableFooter"),
            "ajax": {
                "url": "/Archiv/listFolderWithPagination",
                "type": "POST",
                "data": function (data, meta) {
                    data.folderId = meta.oInit.folderId;
                    data.withFolder = meta.oInit.withFolder;
                    data.itemsToSkip = meta.oInit.itemsToSkip;
                    data.length = meta.oInit.pageLength;
                    duration = new Date().getTime();
                    return JSON.stringify(data);
                },
                "dataSrc": function (data) {
                    REC.log(INFORMATIONAL, "Execution of Service: listFolderWithPagination duration " + (new Date().getTime() - duration) + " ms");
                    fillMessageBox(true);
                    return data.data;
                },
                "dataType": "json",
                "processData": false,
                "contentType": 'application/json;charset=UTF-8'
            },
            "select": {
                "style": 'os'
            },
            "columns": [
                {
                    "class": 'alignCenter details-control awesomeEntity',
                    "orderable": false,
                    "data": null,
                    "defaultContent": '',
                    "width": "12px"
                },
                {
                    "data": "contentStreamMimeType",
                    "title": "Typ",
                    "defaultContent": '',
                    "type": "string",
                    "class": "alignCenter alfrescoTableDragable",
                    "width": "43px"
                },
                {
                    "data": null,
                    "title": "Vorschau",
                    "orderable": false,
                    "defaultContent": '',
                    "type": "string",
                    "class": "alignCenter",
                    "width": "120px"
                },
                {
                    "data": "title",
                    "title": "Titel",
                    "name": "cm:title, cmis:name",
                    "defaultContent": '',
                    "type": "string",
                    "class": "alignLeft alfrescoTableDragable"
                },
                {
                    "data": "documentDateDisplay",
                    "title": "Datum",
                    "name": "my:documentDate, cmis:creationDate",
                    "defaultContent": '',
                    "type": "string",
                    "class": "alignLeft"
                },
                {
                    "data": "person",
                    "title": "Person",
                    "name": "my:person",
                    "defaultContent": '',
                    "type": "string",
                    "class": "alignLeft"
                },
                {
                    "data": "amountDisplay",
                    "title": "Betrag",
                    "name": "my:amount",
                    "defaultContent": '',
                    "type": "string",
                    "class": "alignLeft"
                },
                {
                    "data": "idvalue",
                    "title": "Schlüssel",
                    "name": "my:idvalue",
                    "defaultContent": '',
                    "type": "string",
                    "class": "alignLeft"
                },
                {
                    "data": null,
                    "title": "Aktion",
                    "width": "120px",
                    "class": "alignLeft"
                },
                {
                    "data": "objectID"
                }
            ],
            "columnDefs": [
                {
                    "targets": [5, 6],
                    "visible": true
                },

                {   "targets": [9],
                    "visible": false
                },

                {
                    "targets": [1],
                    "render": function (data, type, row, meta) {
                        try {
                                if (exist(data) && data == "application/pdf" && !meta.settings.oInit.iconView) {
                                    var span = document.createElement("span");
                                    var image = document.createElement('div');
                                    image.id = "alfrescoTableIcon" + row.objectID;
                                    image.className = "alfrescoTableIconEvent alfrescoTableDragable treeDropable fa fa-file-pdf-o fa-15x awesomeEntity";
                                    image.title = "PDF Dokument";
                                    image.draggable = true;
                                    image.style.cursor = "pointer";
                                    $('#alfrescoTabelle tbody').on( 'click', '#' + image.id, function (event) {
                                        openDocument(this, event);
                                    });
                                    span.appendChild(image);
                                    return span.outerHTML;
                                } else
                                    return "";
                        } catch (e) {
                            errorHandler(e);
                        }
                    },
                    "visible": true
                },
                {
                    "targets": [2],
                    "render": function (data, type, row, meta) {
                        try {
                            if (exist(row) && exist(row.nodeRef) && meta.settings.oInit.iconView) {
                                var span = document.createElement("span");

                                span.href = "#";
                                span.style.width = "100px";
                                span.style.height = "100px";
                                var image = document.createElement('img');
                                image.id = "alfrescoTableThumbnail" + row.objectID;
                                image.className = "alfrescoTableThumbnailEvent alfrescoTableDragable treeDropable";
                                image.draggable = true;
                                image.style.cursor = "pointer";
                                image.src = "data:image/png;base64," + executeService({"name": "getThumbnail"}, [
                                        {"name": "documentId", "value": row.objectID}]).data;
                                $('#alfrescoTabelle tbody').on( 'click', '#' + image.id, function (event) {
                                    openDocument(this, event);
                                });
                                span.appendChild(image);
                                return span.outerHTML;
                            } else
                                return "";
                        } catch (e) {
                            errorHandler(e);
                        }
                    },
                    "visible": false
                },
                {
                    "targets": [3],
                    "render": function (data, type, row) {
                        if (exist(data))
                            return data;
                        else if (exist(row.name))
                            return row.name;
                        else
                            return "";
                    },
                    "visible": true
                },
                {
                    "targets": [4],
                    "render": function (data, type, row) {
                        if (row.documentDate) {
                            row.documentDateDisplay = $.datepicker.formatDate("dd.mm.yy", new Date(Number(row.documentDate)));
                        }  else if (row.creationDate)
                            row.documentDateDisplay = $.datepicker.formatDate("dd.mm.yy", new Date(Number(row.creationDate)));
                        else
                            row.documentDateDisplay = "";
                        return row.documentDateDisplay;
                    },
                    "visible": true
                },
                {
                    "targets": [6],
                    "render": function (data, type, row) {
                        if (row.amount) {
                            row.amountDisplay = $.format.number(row.amount, '#,##0.00');
                        } else {
                            row.amountDisplay = "";
                        }
                        return row.amountDisplay;
                    },
                    "visible": true
                },
                {
                    "targets": [8],
                    "render": function(data, types, row) {
                        return alfrescoAktionFieldFormatter(data, types, row).outerHTML;
                    },
                    "orderable": false
                }
            ],
            "language": {
                "info": "Zeigt Einträge _START_ bis _END_ von insgesamt _TOTAL_ \f",
                "emptyTable": "",
                "zeroRecords": "Keine Einträge!",
                "infoEmpty":    "",
                "paginate": {
                    "first": "Erste ",
                    "last":  "Letzte ",
                    "next":  "Nächste ",
                    "previous": "Vorherige "
                },
                "select": {
                    "rows": {
                        "_": " %d Zeilen markiert",
                        "1": " 1 Zeile markiert",
                        "0": ""
                    }
                }
            }
        });

        $("#alfrescoTabelle_info").detach().appendTo('#alfrescoTableFooter');
        $("#alfrescoTabelle_paginate").detach().appendTo('#alfrescoTableFooter');

        // Add event listener for opening and closing details
        $('#dtable2 tbody').on('click', 'td.details-control', function () {
            var tr = $(this).closest('tr');
            var row = alfrescoTabelle.row(tr);

            if (row.child.isShown()) {
                // This row is already open - close it
                row.child.hide();
                $(tr.get(0).childNodes[0]).removeClass('shown');
                calculateTableHeight("alfrescoCenterCenterCenter", "dtable2", alfrescoTabelle, "alfrescoTabelleHeader", "alfrescoTableFooter");
            }
            else {
                // Open this row
                row.child(formatAlfrescoTabelleDetailRow(row.data())).show();
                $(tr.get(0).childNodes[0]).addClass('shown');
                calculateTableHeight("alfrescoCenterCenterCenter", "dtable2", alfrescoTabelle, "alfrescoTabelleHeader", "alfrescoTableFooter");
            }
        });

        // Drag aus Tabelle
        $(document)
            .on('mousedown', '.alfrescoTableDragable', function (event) {
                try {
                    var nodes = [];
                    var selected = alfrescoTabelle.rows( {selected:true} ).data().toArray();
                    //prüfen, ob überhaupt etwas selektiert worden ist
                    if (!selected.length) {
                        var row = alfrescoTabelle.row($(this).closest(('tr')));
                        if (row && row.length)
                         selected.push(row.data());
                    }

                    for ( var index = 0; index < selected.length; ++index) {
                        if (selected[index])
                            nodes.push(selected[index].objectID)
                    }
                    if (nodes.length) {
                        var title = (selected.length > 1 ? (selected.length + " Dokumente") : exist(selected[0].title) ? selected[0].title : selected[0].name);
                        return $.vakata.dnd.start(event, {
                            'jstree': false,
                            'table': "alfrescoTabelle",
                            'obj': $(this),
                            'nodes': nodes
                        }, '<div id="jstree-dnd" class="jstree-default"><i class="jstree-icon jstree-er"></i>' + title + '</div>');
                    }
                } catch (e) {
                    errorHandler(e);
                }
            });
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * baut die Alfresco Folder Tabelle auf.
 */
function loadAlfrescoFolderTable() {
    try {
        alfrescoFolderTabelle = $('#alfrescoFolderTabelle').DataTable({
            "jQueryUI": false,
            "pagingType": "paging_with_jqui_icons",
            "data": [],
            "scrollX": "100%",
            "scrollXInner": "100%",
            // "sScrollY" : calcDataTableHeight(),
            "autoWidth": true,
            "deferRender": true,
            "lengthChange": false,
            "searching": false,
            "processing": true,
            "serverSide": true,
            "folderId" : archivFolderId,
            "withFolder": -1,
            "itemsToSkip": 0,
            "pageLength":  calculateTableHeight("alfrescoCenterCenterNorth", "dtable3", alfrescoFolderTabelle, "alfrescoFolderTabelleHeader", "alfrescoFOlderTableFooter"),
            "ajax": {
                "url": "/Archiv/listFolderWithPagination",
                "type": "POST",
                "data": function (data, meta) {
                    data.folderId = meta.oInit.folderId;
                    data.withFolder = meta.oInit.withFolder;
                    data.itemsToSkip = meta.oInit.itemsToSkip;
                    data.length =  meta.oInit.pageLength;
                    duration = new Date().getTime();
                    return JSON.stringify(data);
                },
                "dataSrc": function (data) {

                    try {
                        REC.log(INFORMATIONAL, "Execution of Service: listFolderWithPagination duration " + (new Date().getTime() - duration) + " ms");
                        //alfrescoFolderTabelle.clear();
                        resizeTable("alfrescoCenterCenterNorth", "dtable3", "alfrescoFolderTabelle", "alfrescoFolderTabelleHeader", "alfrescoFolderTableFooter");
                        //alfrescoFolderTabelle.rows.add(json.data).draw();
                        $.fn.dataTable.makeEditable(alfrescoFolderTabelle, updateInLineFolderFieldFieldDefinition());
                        var tree = $.jstree.reference('#tree');
                        var parent = tree.get_node(data.parent);
                        if (parent) {
                            fillBreadCrumb(parent.data);
                            //$("#tree").jstree(true).refresh_node(objectID);

                            // Knoten einfügen
                            for (var i = 0; i < data.data.length; i++) {
                                if (!exist(tree.get_node(data.data[i].objectID)))
                                    tree.create_node(parent, data.data[i]);
                            }

                            tree.select_node(parent, true, false);
                            tree.open_node(parent);
                        }
                        fillMessageBox(true);
                        return data.data;
                    } catch (e) {
                        errorHandler(e);
                    }
                    
                },
                "dataType": "json",
                "processData": false,
                "contentType": 'application/json;charset=UTF-8'
            },
            "select": {
                "style": 'os'
            },
            "rowCallback": function( row, data ) {
                try {
                    // Cell click
                    $('td', row).on('click', function () {
                        try {
                            if (this.cellIndex == 0) {
                                $("#tree").jstree('deselect_all', true);
                                switchAlfrescoDirectory(data);
                            }
                        } catch (e) {
                            errorHandler(e);
                        }
                    });
                    // Cursor
                    $('td', row).hover(function () {
                        if (this.cellIndex == 0)
                            $(this).css('cursor', 'pointer');
                    }, function () {
                        $(this).css('cursor', 'auto');

                    });
                } catch (e) {
                    errorHandler(e);
                }
            },
            // "iDisplayLength": Math.max(Math.floor((verteilungLayout.state.west.innerHeight - 24 - 26 - 20) / 29), 1),
            "order": [[2, 'desc']],
            "columns": [
                {
                    "class": 'alignCenter folder-control awesomeEntity treeDropable',
                    "orderable": false,
                    "data": null,
                    "defaultContent": '',
                    "width": "30px"
                },
                {
                    "data": "name",
                    "title": "Name",
                    "name" : "cm:title",
                    "defaultContent": '',
                    "type": "string",
                    "createdCell": function(tableData, value, data, row, column) {
                        if (data.objectID == alfrescoRootFolderId ||
                            data.objectID == archivFolderId ||
                            data.objectID == fehlerFolderId ||
                            data.objectID == unknownFolderId ||
                            data.objectID == doubleFolderId ||
                            data.objectID == documentFolderId ||
                            data.objectID == inboxFolderId)
                            $(tableData).addClass("read_only");
                        else
                            $(tableData).removeClass("read_only");
                    },
                    "class": "alignLeft alfrescoFolderTableDragable treeDropable"
                },
                {
                    "data": "description",
                    "title": "Beschreibung",
                    "name" : "cm:description",
                    "defaultContent": '',
                    "type": "string",
                    "class": "alignLeft alfrescoFolderTableDragable treeDropable"
                },
                {
                    "title": "Aktion",
                    "data": null,
                    "width": "120px",
                    "class": "alignLeft",
                    "orderable": false
                }
            ],
            "columnDefs": [
                {
                    "targets": [0],
                    "orderable": false
                },
                {
                    "targets": [1, 2],
                    "visible": true
                },
                {
                    "targets": [3],
                    "render": function(data, type, row) {
                        return alfrescoFolderAktionFieldFormatter(data, type, row).outerHTML;
                    },
                    "orderable": false
                }
            ],
            "language": {
                "info": "Zeigt Einträge _START_ bis _END_ von insgesamt _TOTAL_ \f",
                "emptyTable": "",
                "zeroRecords": "Keine Einträge!",
                "infoEmpty":    "",
                "paginate": {
                    "first": "Erste ",
                    "last":  "Letzte ",
                    "next":  "Nächste ",
                    "previous": "Vorherige "
                },
                "select": {
                    "rows": {
                        "_": " %d Zeilen markiert",
                        "1": " 1 Zeile markiert",
                        "0": ""
                    }
                }
            }
        });

        // Drag aus Tabelle
        $(document)
            .on('mousedown', '.alfrescoFolderTableDragable', function (event) {
                try {
                    var nodes = [];
                    var selected = alfrescoFolderTabelle.rows( {selected:true} ).data().toArray();
                    //prüfen, ob überhaupt etwas selektiert worden ist
                    if (!selected.length) {
                        var row = alfrescoFolderTabelle.row($(this).closest(('tr')));
                        if (row && row.length)
                            selected.push(row.data());
                    }

                    for( var index = 0; index < selected.length; ++index) {
                        if (selected[index])
                            nodes.push(selected[index].objectID)
                    }
                    if (nodes.length) {
                        var title = (selected.length > 1 ? (selected.length + " Ordner") : exist(selected[0].title) ? selected[0].title : selected[0].name);
                        return $.vakata.dnd.start(event, {
                            'jstree': false,
                            'table': "alfrescoFolderTabelle",
                            'obj': $(this),
                            'nodes': nodes
                        }, '<div id="jstree-dnd" class="jstree-default"><i class="jstree-icon jstree-er"></i>' + title + '</div>');
                    }
                } catch (e) {
                    errorHandler(e);
                }
            });

    } catch (e) {
        errorHandler(e);
    }
    $("#alfrescoFolderTabelle_info").detach().appendTo('#alfrescoFolderTableFooter');
    $("#alfrescoFolderTabelle_paginate").detach().appendTo('#alfrescoFolderTableFooter');
}

/**
 * baut die die Tabelle für die Suchergebnisse auf.
 */
function loadAlfrescoSearchTable() {

    /**
     * öffnet ein Dokument
     * @param obj
     * @param event
     */
    function openDocument(obj, event) {
        try {
            event.preventDefault();
            event.stopImmediatePropagation();
            var erg = executeService({"name": "openDocument", "errorMessage": "Dokument konnten nicht geöffnet werden!"}, [
                {"name": "documentId", "value": alfrescoSearchTabelle.row($(obj).closest('tr')).data().objectID}
            ]);
            if (erg.success) {
                var  file = new Blob([base64DecToArr(erg.data)], { type: erg.mimeType });
                var fileURL = URL.createObjectURL(file);
                window.open(fileURL);
            }

        } catch (e) {
            errorHandler(e);
        }
    }


    try {
        var duration;
        $.fn.dataTable.moment('DD.MM.YY');
        alfrescoSearchTabelle = $('#alfrescoSearchTabelle').DataTable({
            "jQueryUI": false,
            "pagingType": "paging_with_jqui_icons",
            "data": [],
            "scrollX": "100%",
            "scrollXInner": "100%",
            "autoWidth": false,
            "deferRender": true,
            "lengthChange": false,
            "searching": false,
            "order": [[3, 'desc']],
            "processing": true,
            "serverSide": true,
            "cmisQuery" : "",
            "itemsToSkip": 0,
            "iconView": false,
            "pageLength": 1, // kann hier 1 sein, weil im Root keine Dokumente sind
            "ajax": {
                "url": "/Archiv/findDocumentWithPagination",
                type: "POST",
                data: function (data, meta) {
                    data.filePath = meta.oInit.itemsToSkip;
                    data.cmisQuery = meta.oInit.cmisQuery;
                    duration = new Date().getTime();
                    return JSON.stringify(data);
                },
                dataSrc: function (data) {
                    REC.log(INFORMATIONAL, "Execution of Service: findFolderWithPagination duration " + (new Date().getTime() - duration) + " ms");
                    fillMessageBox(true);
                    return data.data;
                },
                dataType: "json",
                processData: false,
                contentType: 'application/json;charset=UTF-8'
            },
            "columns": [

                {
                    "class": 'alignCenter details-control awesomeEntity',
                    "orderable": false,
                    "data": null,
                    "defaultContent": '',
                    "width": "12px"
                },
                {
                    "data": "contentStreamMimeType",
                    "title": "Typ",
                    "defaultContent": '',
                    "type": "string",
                    "class": "alignCenter",
                    "width": "43px"
                },
                {
                    "data": null,
                    "title": "Vorschau",
                    "orderable": false,
                    "defaultContent": '',
                    "type": "string",
                    "class": "alignCenter",
                    "width": "120px"
                },
                {
                    "data": "title",
                    "title": "Titel",
                    "name": "cm:title, cmis:name",
                    "defaultContent": '',
                    "type": "string",
                    "class": "alignLeft"
                },
                {
                    "data": "documentDateDisplay",
                    "title": "Datum",
                    "name": "my:documentDate, cmis:creationDate",
                    "defaultContent": '',
                    "type": "string",
                    "class": "alignLeft"
                },
                {
                    "data": "person",
                    "title": "Person",
                    "name": "my:person",
                    "defaultContent": '',
                    "type": "string",
                    "class": "alignLeft"
                },
                {
                    "data": "amountDisplay",
                    "title": "Betrag",
                    "name": "my:amount",
                    "defaultContent": '',
                    "type": "string",
                    "class": "alignLeft"
                },
                {
                    "data": "idvalue",
                    "title": "Schlüssel",
                    "name": "my:idvalue",
                    "defaultContent": '',
                    "type": "string",
                    "class": "alignLeft"
                },
                {
                    "data": null,
                    "title": "Aktion",
                    "width": "120px",
                    "class": "alignLeft"
                },
                {
                    "data": "objectID"
                }
            ],
            "columnDefs": [
                {
                    "targets": [5, 6],
                    "visible": true
                },

                {   "targets": [9],
                    "visible": false
                },

                {
                    "targets": [1],
                    "render": function (data, type, row, meta) {
                        try {
                            if (exist(data) && data == "application/pdf" && !meta.settings.oInit.iconView) {
                                var span = document.createElement("span");
                                var image = document.createElement('div');
                                image.id = "alfrescoSearchTableIcon" + row.objectID;
                                image.className = "alfrescoSearchTableIconEvent fa fa-file-pdf-o fa-15x awesomeEnity";
                                image.title = "PDF Dokument";
                                image.draggable = true;
                                image.style.cursor = "pointer";
                                $('#alfrescoSearchTabelle tbody').on( 'click', '#' + image.id, function (event) {
                                    openDocument(this, event);
                                });
                                span.appendChild(image);
                                return span.outerHTML;
                            } else
                                return "";
                        } catch (e) {
                            errorHandler(e);
                        }
                    },
                    "visible": true
                },
                {
                    "targets": [2],
                    "render": function (data, type, row, meta) {
                        try {
                            if (exist(data) && meta.settings.oInit.iconView) {
                                var span = document.createElement("span");

                                span.href = "#";
                                span.style.width = "100px";
                                span.style.height = "100px";
                                var image = document.createElement('img');
                                image.id = "alfrescoSearchTableThumbnail" + row.objectID;
                                image.className = "alfrescoSearchTableThumbnailEvent";
                                image.draggable = true;
                                image.style.cursor = "pointer";
                                image.src = "data:image/png;base64," + executeService({"name": "getThumbnail"}, [
                                        {"name": "documentId", "value": row.objectID}]).data;
                                $('#alfrescoSearchTabelle tbody').on( 'click', '#' + image.id, function (event) {
                                    openDocument(this, event);
                                });
                                span.appendChild(image);
                                return span.outerHTML;
                            } else
                                return "";
                        } catch (e) {
                            errorHandler(e);
                        }
                    },
                    "visible": false
                },
                {
                    "targets": [3],
                    "render": function (data, type, row) {
                        if (exist(data))
                            return data;
                        else if (exist(row.name))
                            return row.name;
                        else
                            return "";
                    },
                    "visible": true
                },
                {
                    "targets": [4],
                    "render": function (data, type, row) {
                        if (row.documentDate) {
                            row.documentDateDisplay = $.datepicker.formatDate("dd.mm.yy", new Date(Number(row.documentDate)));
                        }  else if (row.creationDate)
                            row.documentDateDisplay = $.datepicker.formatDate("dd.mm.yy", new Date(Number(row.creationDate)));
                        else
                            row.documentDateDisplay = "";
                        return row.documentDateDisplay;
                    },
                    "visible": true
                },
                {
                    "targets": [6],
                    "render": function (data, type, row) {
                        if (row.amount) {
                            row.amountDisplay = $.format.number(row.amount, '#,##0.00');
                        } else {
                            row.amountDisplay = "";
                        }
                        return row.amountDisplay;
                    },
                    "visible": true
                },
                {
                    "targets": [8],
                    "render": function(data, type, row) {
                        
                        var container = alfrescoAktionFieldFormatter(data, type, row);
                        var image = document.createElement("div");
                        image.href = "#";
                        image.className = "detailAim";
                        image.style.backgroundImage = "url(./images/ziel.png)";
                        image.title = "Dokument im Ordner anzeigen";
                        image.style.cursor = "pointer";
                        image.style.width = "16px";
                        image.style.height = "16px";
                        image.style.cssFloat = "left";
                        image.style.marginRight = "5px";
                        container.appendChild(image);
                        return container.outerHTML;
                    },
                    "orderable": false
                }
            ],
            "language": {
                "info": "Zeigt Einträge _START_ bis _END_ von insgesamt _TOTAL_ \f",
                "emptyTable": "Keine Ergebnisse gefunden",
                "zeroRecords": "Keine Einträge!",
                "infoEmpty":   "",
                "paginate": {
                    "first": "Erste ",
                    "last":  "Letzte ",
                    "next":  "Nächste ",
                    "previous": "Vorherige "
                },
                "select": {
                    "rows": {
                        "_": " %d Zeilen markiert",
                        "1": " 1 Zeile markiert",
                        "0": ""
                    }
                }
            }
        });

        $("#alfrescoSearchTabelle_info").detach().appendTo('#alfrescoSearchTableFooter');
        $("#alfrescoSearchTabelle_paginate").detach().appendTo('#alfrescoSearchTableFooter');

        // Add event listener for opening and closing details
        $('#dtable4 tbody').on('click', 'td.details-control', function () {
            var tr = $(this).closest('tr');
            var row = alfrescoSearchTabelle.row(tr);

            if (row.child.isShown()) {
                // This row is already open - close it
                row.child.hide();
                tr.removeClass('shown');
                calculateTableHeight("searchCenter", "dtable4", alfrescoSearchTabelle, "alfrescoSearchTabelleHeader", "alfrescoSearchTableFooter");
            }
            else {
                // Open this row
                row.child(formatAlfrescoTabelleDetailRow(row.data())).show();
                tr.addClass('shown');
                calculateTableHeight("searchCenter", "dtable4", alfrescoSearchTabelle, "alfrescoSearchTabelleHeader", "alfrescoSearchTableFooter");
            }
        });

    } catch (e) {
        errorHandler(e);
    }
}


/**
 * lädt die Tabelle für den Verteilungstab
 */
function loadVerteilungTable() {
    try {
        $('#dtable').html('<table cellpadding="0" cellspacing="0" border="0" class="display" id="tabelle"></table>');
        tabelle = $('#tabelle').DataTable({
            "jQueryUI": false,
            "pagingType": "paging_with_jqui_icons",
            "data": [],
            "scrollX": "100%",
            "scrollXInner": "100%",
            "autoWidth": true,
            "lengthChange": false,
            "searching": false,
            //"pageLength": Math.max(Math.floor((verteilungLayout.state.west.innerHeight - 24 - 26 - 20) / 29), 1),
            "columns": [
                {
                    "class": 'alignCenter details-control awesomeEntity',
                    "orderable": false,
                    "data": null,
                    "defaultContent": '',
                    "width": "35px"
                },
                {
                    "title": "Name",
                    "type": "string",
                    "class": "alignLeft"
                },
                {
                    "title": "Dokumenttyp",
                    "type": "string",
                    "class": "alignLeft"
                },
                {
                    "title": "Ergebnis",
                    "width": "102px",
                    "class": "alignLeft"
                },
                {
                    "title": "Id"
                },
                {
                    "title": "Fehler"
                }
            ],
            "columnDefs": [
                {
                    "targets": [0],
                    "sortable": false
                },
                {
                    "targets": [1, 2, 3],
                    "visible": true
                },
                {
                    "targets": [3],
                    "render": function (data, types, row) {
                        return imageFieldFormatter(data, types, row).outerHTML;
                    },
                    "sortable": false
                },
                {
                    "targets": [4, 5],
                    "visible": false
                }
            ],
            "language": {
                "info": "Zeigt Einträge _START_ bis _END_ von insgesamt _TOTAL_ \f",
                "emptyTable": "Keine Ergebnisse gefunden",
                "zeroRecords": "Keine Einträge!",
                "infoEmpty": "",
                "paginate": {
                    "first": "Erste ",
                    "last": "Letzte ",
                    "next": "Nächste ",
                    "previous": "Vorherige "
                }
            }
        });
        // Add event listener for opening and closing details
        $('#tabelle tbody').on('click', 'td.details-control', function () {
            var tr = $(this).closest('tr');
            var row = tabelle.row(tr);

            if (row.child.isShown()) {
                // This row is already open - close it
                row.child.hide();
                tr.removeClass('shown');
                calculateTableHeight("verteilungWest", "dtable", tabelle, "verteilungTabelleHeader", "verteilungTableFooter");
            }
            else {
                // Open this row
                row.child(formatVerteilungTabelleDetailRow(row.data())).show();
                tr.addClass('shown');
                calculateTableHeight("verteilungWest", "dtable", tabelle, "verteilungTabelleHeader", "verteilungTableFooter");

            }
        });
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * formatiert die Iconspalte in der AlfrescoFolderTabelle
 * @param data
 * @param type
 * @param full
 * @return {string}
 */
function alfrescoFolderAktionFieldFormatter(data, type, full) {
    try {

        var container = document.createElement("div");
        var image;
        var inner1;
        var inner2;

        // Ordner bearbeiten
        image = document.createElement("i");
        image.href = "#";
        image.className = "folderEdit fa fa-pencil fa-15x awesomeEntity";
        image.title = "Ordner Details bearbeiten";
        image.style.cursor = "pointer";
        image.style.cssFloat = "left";
        image.style.marginRight = "5px";
        container.appendChild(image);
        
        // neuen Ordner im ausgewählten Ordner anlegen
        if (data.objectID != alfrescoRootFolderId &&
            data.objectID != archivFolderId &&
            data.objectID != fehlerFolderId &&
            data.objectID != unknownFolderId &&
            data.objectID != doubleFolderId &&
            data.objectID != inboxFolderId) {
            image = document.createElement("i");
            image.href = "#";
            //image.style.fontSize ="xx-small";
            image.className = "folderCreate fa-stack fa-09x";
            inner1 = document.createElement("i");
            inner1.className = "fa fa-folder-o fa-stack-2x awesomeEntity" ;
            inner2 = document.createElement("i");
            inner2.className = " fa fa-plus fa-stack-1x awesomeAction";
            inner2.style.color = "green";
            image.appendChild(inner1);
            image.appendChild(inner2);
            image.title = "neuen Ordner anlegen";
            image.style.cursor = "pointer";
            image.style.cssFloat = "left";
            image.style.marginRight = "5px";
            container.appendChild(image);
        }
        
        // ausgewählten Ordner löschen
        if (data.objectID != alfrescoRootFolderId &&
            data.objectID != archivFolderId &&
            data.objectID != fehlerFolderId &&
            data.objectID != unknownFolderId &&
            data.objectID != doubleFolderId &&
            data.objectID != inboxFolderId &&
            data.objectID != documentFolderId) {
            image = document.createElement("i");
            image.href = "#";
            //image.style.fontSize ="xx-small";
            image.className = "folderRemove fa-stack fa-09x";
            inner1 = document.createElement("i");
            inner1.className = "fa fa-folder-o fa-stack-2x awesomeEntity" ;
            inner2 = document.createElement("i");
            inner2.className = " fa fa-remove fa-stack-1x awesomeAction";
            inner2.style.color = "red";
            image.appendChild(inner1);
            image.appendChild(inner2);
            image.title = "Ordner löschen";
            image.style.cursor = "pointer";
            image.style.cssFloat = "left";
            image.style.marginRight = "5px";
            container.appendChild(image);
        }

        return container;
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * formatiert die Iconspalte in der AlfrescoTabelle
 * @param data
 * @param type
 * @param full
 * @returns {string}
 */
function alfrescoAktionFieldFormatter(data, type, full) {
    try {
        var container = document.createElement("div");
        var image = document.createElement("i");
        image.href = "#";
        image.className = "detailEdit fa fa-pencil fa-15x awesomeEntity";
        image.title = "Details bearbeiten";
        image.style.cursor = "pointer";
        image.style.cssFloat = "left";
        image.style.marginRight = "5px";
        container.appendChild(image);

        image = document.createElement("i");
        image.href = "#";

        if (data.commentCount > 0) {
            image.style.cursor = "pointer";
            if (data.commentCount == 1)
                image.className = "showComments fa fa-comment fa-15x awesomeEntity";
            else
                image.className = "showComments fa fa-comments fa-15x awesomeEntity";
        }
        else {
             image.className = "showComments fa fa-comment-o fa-15x awesomeEntity";
            image.style.cursor = "none";
        }
        image.title = "Kommentare";
        image.style.cssFloat = "left";
        image.style.marginRight = "5px";
        container.appendChild(image);

        image = document.createElement("i");
        image.href = "#";
        image.className = "moveDocument fa fa-files-o fa-15x awesomeEntity";
        image.title = "Dokument verschieben";
        image.style.cursor = "pointer";
        image.style.cssFloat = "left";
        image.style.marginRight = "5px";
        container.appendChild(image);

        image = document.createElement("i");
        image.href = "#";
        image.className = "deleteDocument fa fa-trash fa-15x awesomeEntity";
        image.title = "Dokument löschen";
        image.style.cursor = "pointer";
        image.style.cssFloat = "left";
        image.style.marginRight = "5px";
        container.appendChild(image);

        image = document.createElement("i");
        image.href = "#";
        image.className = "rulesDocument fa fa-wpforms fa-15x awesomeEntity";
        image.title = "Dokument Regel erstellen";
        image.style.cursor = "pointer";
        image.style.cssFloat = "left";
        image.style.marginRight = "5px";
        container.appendChild(image);

        return container;
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * formatiert die Iconspalte in der Tabelle
 * @param data
 * @param type
 * @param full
 * @return {string}
 */
function imageFieldFormatter(data, type, full) {
    try {
        var container = document.createElement("div");
        var image = document.createElement("div");
        image.href = "#";
        image.className = "run";
        if (full[3].error) {
            image.style.backgroundImage = "url(./images/error.png)";
            image.title = "Verteilung fehlerhaft";
        } else {
            image.style.backgroundImage = "url(./images/ok.png)";
            image.title = "Verteilung erfolgreich";
        }
        image.style.cursor = "pointer";
        image.style.width = "16px";
        image.style.height = "16px";
        image.style.cssFloat = "left";
        image.style.marginRight = "5px";
        container.appendChild(image);
        image = document.createElement("div");
        image.href = "#";
        image.className = "glass";
        image.title = "Ergebnis anzeigen";
        image.style.backgroundImage = "url(./images/glass.png)";
        image.style.width = "16px";
        image.style.height = "16px";
        image.style.cursor = "pointer";
        image.style.cssFloat = "left";
        image.style.marginRight = "5px";
        container.appendChild(image);
        image = document.createElement("div");
        image.href = "#";
        image.className = "loeschen";
        image.title = "Ergebnis löschen";
        if (daten[full[1]]["notDeleteable"] != "true") {
            image.style.backgroundImage = "url(./images/delete.png)";
            image.style.cursor = "pointer";
        }
        else {
            image.style.backgroundImage = "url(./images/delete-bw.png)";
            image.style.cursor = "not-allowed";
        }
        image.style.width = "16px";
        image.style.height = "16px";
        image.style.cssFloat = "left";
        image.style.marginRight = "5px";
        container.appendChild(image);
        image = document.createElement("div");
        image.className = "pdf";
        if (full[1].toLowerCase().endsWith(".pdf")) {
            image.style.backgroundImage = "url(./images/pdf.png)";
            image.style.cursor = "pointer";
        } else {
            image.style.backgroundImage = "url(./images/pdf-bw.png)";
            image.style.cursor = "not-allowed";
        }
        image.style.cssFloat = "left";
        image.style.width = "16px";
        image.style.height = "16px";
        image.style.marginRight = "5px";
        image.title = "PDF anzeigen";
        container.appendChild(image);
        image = document.createElement("div");
        image.className = "moveToInbox";
        image.style.backgroundImage = "url(./images/move-file.png)";
        image.style.cursor = "pointer";
        image.style.cssFloat = "left";
        image.style.width = "16px";
        image.style.height = "16px";
        // image.style.marginRight = "5px";
        image.title = "Zur Inbox verschieben";
        container.appendChild(image);
        return container;
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * formatiert die Fehlerdetails in der zusätzlichen Zeile(n) der VerteilungsTabelle
 * @param data         Das Data Object der Zeile
 * @returns {string}   HTML für die extra Zeile
 */
function formatVerteilungTabelleDetailRow(data) {
    var sOut = '<div class="innerDetails" style="overflow: auto; width: 100%; " ><table>' +
        '<tr><tr style="height: 0px;" > '+
        '<th style="width: 100px; padding-top: 0px; padding-bottom: 0px; border-top-width: 0px; border-bottom-width: 0px; height: 0px; font-size: 12px"' +
        'colspan="1" rowspan="1" tabindex="0" class="control center">Fehler</th>' +
        '<th style="width: auto; padding-left: 10px; padding-top: 0px; padding-bottom: 0px; border-top-width: 0px; border-bottom-width: 0px; height: 0px; font-size: 12px"' +
        'colspan="1" rowspan="1" tabindex="0" class="alignLeft">Beschreibung</th></tr><td>';
    var txt = "<tr>";
    for ( var i = 0; i < data[5].length; i++) {
        txt = txt + "<td class='alignCenter' style='font-size: 11px; padding-top: 0px; padding-bottom: 0px'>" + (i+1) + "</td><td style='font-size: 11px; padding-top: 0px; padding-bottom: 0px'>" + data[5][i] + "</td>";
        txt = txt + "</tr>";
    }
    sOut = sOut + txt;
    sOut += '</table></div>';
    return sOut;
}

/**
 * formatiert die zusätzlichen Zeile(n) der AlfrescoTabelle
 * @param data         Das Data Object der Zeile
 * @returns {string}   HTML für die extra Zeile
 */
function formatAlfrescoTabelleDetailRow(data) {
    return 'Name: ' + data.name + ' erstellt am: ' + $.formatDateTime('dd.mm.yy hh:ii:ss', new Date(Number(data.creationDate))) + ' von: ' + data.createdBy + (data.lastModificationDate == data.creationDate ? '' : ' modifiziert am: ' + $.formatDateTime('dd.mm.yy hh:ii:ss', new Date(Number(data.lastModificationDate))) + ' von: ' + data.lastModifiedBy) + ' Version: ' + data.versionLabel + ' ' + (exist(data.checkinComment) ? data.checkinComment : '');
}

/**
 * füllt die BreadCrumb Leiste
 * @param data          der aktuelle Folder
 */
function fillBreadCrumb(data) {
    try {
        var object;
        var id;
        var parentObj;
        var fill = true;
        var tree = $("#tree").jstree(true);
        var oldLi = $('#breadcrumblist');
        if (exist(oldLi))
            oldLi.remove();
        var container = $('#breadcrumb');
        var ul = document.createElement('ul');
        ul.id = 'breadcrumblist';
        do {
            if(exist(data.path)) {
                object = data.path.split('/');
                id = data.objectID;
                parentObj = data.parentId;
                name = data.name;

                var li = document.createElement('li');
                li.data = {
                    'objectID': id,
                    'path': object.join('/'),
                    'name': name,
                    'parentId': parentObj
                };
                li.id = id;
                li.onclick = function () {
                    tree.deselect_all(false);
                    switchAlfrescoDirectory(this.data);
                };
                $.data(li, "data", data);
                var a = document.createElement('a');
                a.href = '#';
                a.text = name;
                li.appendChild(a);
                // prüfen, ob ein Parent da ist
                if (parentObj == null)
                    fill = false;
                else {
                    // Daten des Parents
                    data = tree.get_node(parentObj).data;
                    // wenn die nicht existieren sind wir im Root Knoten und können hier abbrechen
                    if (!exist(data))
                        fill = false
                }
                
                ul.insertBefore(li, ul.firstChild);
            }
        } while (fill);
        container.append(ul);
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * liefert die Felddefinitionen fürs InlineEdititing der Dokumenttabellen
 * @return  JSON Struktur mit der Definition
 */
function updateInLineDocumentFieldDefinition () {
    return {
        "fnShowError" : function(text, aktion){
            message("Fehler", text);
        },
        "aoColumns": [ null,
            null,
            {
                "placeholder": "",
                "tooltip": 'Titel des Dokumentes'
            },
            {
                "placeholder": "",
                "tooltip": 'Datum des Dokumentes',
                "type": 'datepicker',
                "datepicker": {
                    "dateFormat": "dd.mm.yy"
                }
            },
            {
                "placeholder": "",
                "tooltip": 'Zugeordnete Person',
                "loadtext": 'lade...',
                "type": 'select',
                "onblur": 'submit',
                "data": "{'':'Bitte auswählen...', 'Klaus':'Klaus','Katja':'Katja','Till':'Till', 'Kilian':'Kilian'}"
            },
            {
                "placeholder": "",
                "tooltip": 'Rechnungsbetrag des Dokumentes'
            },
            {
                "placeholder": "",
                "tooltip": 'Identifikationsschlüssel des Dokumentes'
            },
            null
        ],
        sUpdateURL: updateInlineDocument
    };
}


/**
 * liefert die Felddefinitionen fürs InlineEdititing der Foldertabellen
 * @return  JSON Struktur mit der Definition
 */
function updateInLineFolderFieldFieldDefinition() {
    return {
        "fnShowError": function (text, aktion) {
            message("Fehler", text);
        },
        "aoColumns": [null,
            {
                "placeholder": ""
            },
            {
                "placeholder": ""
            },
            null
        ],
        "sUpdateURL": updateInlineFolder
    };
}

/**
 * führt das Inline editieren in der Foldertabellen durch
 * @param value         der neue Value für das Feld
 * @param settings      die Settings
 * @return {*}
 */
function updateInlineFolder (value, settings) {
    
    try {
        var extraProperties;
        var changed = false;
        var oldValue = "";
        var data = alfrescoFolderTabelle.row($(this).closest('tr')).data();
        switch (this.cellIndex) {
            case 1: {                           // Name geändert
                if (value != data.name) {
                    if (data.name)
                        oldValue = data.name;
                    data.name = value;
                    changed = true;
                }
                break;
            }
            case 2: {                           // Beschreibung geändert
                if (value != data.description) {
                    if (data.description)
                        oldValue = data.description;
                    data.description = value;
                    changed = true;
                }
                break;
            }
        }
        if (changed) {
            var erg = editFolder(data, data.objectID);
            if (!erg.success) {
                value = erg.error;
            }
        }
        return value;
    } catch (e) {
        errorHandler(e);
    }
}


/**
 * führt das Inline editieren in den Dokumententabellen durch
 * @param value         der neue Value für das Feld
 * @param settings      die Settings
 * @return {*}
 */
function updateInlineDocument(value, settings) {
    try {
        var changed = false;
        var convValue;
        var oldValue = "";
        var row = $(this.parentElement.parentElement.parentElement).DataTable().row(this);
        var data = row.data();
        switch (this.cellIndex) {
            case 2: {                                // Titel geändert
                if ( value != data.title) {
                    if (data.titel)
                        oldValue = data.title;
                    data.title = value;
                    changed = true;
                }
                break;
            }
            case 3: {                               // Datum geändert
                convValue = $.datepicker.parseDate("dd.mm.yy", value).getTime();
                if (convValue != data.documentDate){
                    if (data.documentDateDisplay)
                        oldValue = data.documentDateDisplay;
                    data.documentDate = convValue;
                    changed = true;
                }
                break;
            }
            case 4: {                              // Person geändert
                if (value != data.person){
                    if (data.person)
                        oldValue = data.person;
                    data.person = value;
                    changed = true;
                }
                break;
            }
            case 5: {                             // Betrag geändert
                convValue = parseFloat(value.replace(/\./g, '').replace(/,/g, "."));
                if (convValue != data.amount) {
                    if (data.amountDisplay)
                        oldValue = data.amountDisplay;
                    data.amount = convValue;
                    changed = true;
                }
                break;
            }
            case 6:{                             // Id geändert
                if (value != data.idvalue){
                    if (data.idvalue)
                        oldValue = data.idvalue;
                    data.idvalue = value;
                    changed = true;
                }
                break;
            }
        }
        if (changed) {
            var erg = editDocument(data, data.objectID);
            if (!erg.success) {
                value = erg.error;
            }
        }
        return value;
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * ändert ein Dokument
 * @param input             die neu einzutragenden Daten
 * @param id                die Id des Objektes
 * @return                  true, wenn erfolgreich   
 **/
function editDocument(input, id) {
    var erg = {"success": false};
    try {
        var extraProperties = {
            'P:cm:titled': {
                'cm:title': input.title,
                'cm:description': input.description
            },
            'D:my:archivContent': {
                'my:documentDate': input.documentDate,
                'my:person': input.person
            },
            'P:my:amountable': {'my:amount': input.amount, "my:tax": input.tax},
            'P:my:idable': {'my:idvalue': input.idvalue}
        };
        var done = function (json) {
            if (json.success) {
                var data = json.data;
                // Tabelle updaten
                var row = alfrescoTabelle.row('#' + data.objectID);
                if (row && row.length)
                    row.data(data).invalidate();
                // Suchergebnis eventuell updaten
                row = alfrescoSearchTabelle.row('#' + data.objectID);
                if (row && row.length)
                    row.data(data).invalidate();
            }

        };
        erg = executeService({"name": "updateProperties", "callback": done, "errorMessage": "Dokument konnte nicht aktualisiert werden!", "ignoreError": true}, [
            {"name": "documentId", "value": id},
            {"name": "extraProperties", "value": extraProperties}
        ]);
     } catch (e) {
        errorHandler(e);
    }
    return erg;
}

/**
 * löscht ein Dokument
 */
function deleteDocument() {
    try {
        var origData = $("#dialogBox").alpaca().data;
        var done = function (json) {
            if (json.success) {
                var row = alfrescoTabelle.row('#' + origData.objectId);
                if (row && row.length)
                    row.remove().draw(false);
                row = alfrescoSearchTabelle.row('#' + data.objectId);
                if (row && row.length)
                    row.remove().draw(false);
            }
        };
        var erg = executeService({"name": "deleteDocument", "callback": done, "errorMessage": "Dokument konnte nicht gelöscht werden!"}, [
            {"name": "documentId", "value": origData.objectID}
        ]);
     } catch (e) {
        errorHandler(e);
    }
}

/**
 * erzeugt einen neuen Ordner
 * @param input         die Daten des neuen Ordners
 * @param origData      die übergebenen Daten
 */
function createFolder(input, origData) {
    try {
        var extraProperties = {
            'cmis:folder': {
                'cmis:objectTypeId': 'cmis:folder',
                'cmis:name': input.name
            },
            'P:cm:titled': {
                'cm:title': input.title,
                'cm:description': input.description
            }
        };
        var done = function (json) {
            if (json.success) {
                var lastElement = $("#breadcrumblist").children().last();
                var newData = json.data;
                var tree = $.jstree.reference('#tree');
                // Tree updaten
                var node = tree.get_node(newData.parentId);
                if (node) {
                    tree.create_node(node, buildObjectForTree(newData));
                }
                // Tabelle updaten
                if (lastElement && lastElement.get(0).id == newData.parentId) {
                    alfrescoFolderTabelle.rows.add([newData]).draw();
                }
                // BreadCrumb aktualisieren
                if (lastElement)
                    fillBreadCrumb(lastElement.data().data);
            }
        };
        var erg = executeService({"name": "createFolder", "callback": done, "errorMessage": "Ordner konnte nicht erstellt werden!"}, [
            {"name": "documentId", "value": origData.objectId},
            {"name": "extraProperties", "value": extraProperties}
        ]);
     } catch (e) {
        errorHandler(e);
    }
}

/**
 * editiert einen Ordner
 * @param input         die neuen Daten des Ordners
 * @param id            die Id des Ordners
 * @return              true, wenn erfolgreich
 */
function editFolder(input, id) {
    var erg = {"success": false};
    try {
        var extraProperties = {
            'cmis:folder': {
                'cmis:objectTypeId': 'cmis:folder',
                'cmis:name': input.name
            },
            'P:cm:titled': {
                'cm:title': input.title,
                'cm:description': input.description
            }
        };
        var done = function (json) {
            if (json.success) {
                var lastElement = $("#breadcrumblist").children().last();
                var newData = json.data;
                // Tree updaten
                var tree = $.jstree.reference('#tree');
                var node = tree.get_node(newData.objectID);
                if (node) {
                    tree.rename_node(node, newData.name);
                    node.data = newData;
                }
                // Tabelle updaten
                if (lastElement && lastElement.get(0).id == newData.parentId) {
                    var row = alfrescoFolderTabelle.row('#' + newData.objectID);
                    if (row && row.length) {
                        row.data(newData).invalidate();
                    }
                }
                // BreadCrumb aktualisieren
                if (lastElement && lastElement.get(0).id == id) {
                    fillBreadCrumb(input);
                } else if (lastElement)
                    fillBreadCrumb(lastElement.data().data);
            }

        };
        erg = executeService({"name": "updateProperties", "callback": done, "errorMessage": "Ordner konnte nicht aktualisiert werden!", "ignoreError": true}, done,[
            {"name": "documentId", "value": id},
            {"name": "extraProperties", "value": extraProperties}
        ]);
      } catch (e) {
        errorHandler(e);
    }
    return erg;
}

/**
 * löscht einen Ordner
 * TODO Was passiert hier mit den eventuell vorhandenen Suchergebnissen
 * TODO der sollte die Dialogbox hier nicht kennen
 */
function deleteFolder() {
    try {
        var origData = $("#dialogBox").alpaca().data;
        var done = function (json) {
            if (json.success) {
                var lastElement = $("#breadcrumblist").children().last();
                // Tree updaten
                var tree = $.jstree.reference('#tree');
                tree.delete_node(origData.objectID);
                // Tabelle updaten
                if (lastElement && lastElement.get(0).id == origData.parentId) {
                    var row = alfrescoFolderTabelle.row('#' + origData.objectID);
                    if (row && row.length) {
                        row.remove().draw();
                    }
                }
                // der aktuelle Ordner ist der zu löschende
                if (lastElement && lastElement.get(0).id == origData.objectID) {
                    tree.select_node(origData.parentId);
                } else {
                    // BreadCrumb aktualisieren
                    if (lastElement)
                        fillBreadCrumb(lastElement.data().data);
                }
            }

        }
        var erg = executeService({"name": "deleteFolder", "callback": done, "errorMessage": "Ordner konnte nicht gelöscht werden!"}, [
            {"name": "documentId", "value": origData.objectID}
        ]);
    } catch (e) {
        errorHandler(e);
    }
}


/**
 * führt die Aktualisierungen für eine Verzeichniswechsel im Alfresco durch
 * @param data      das Datenobjekt des ausgewählten Folders
 */
function switchAlfrescoDirectory(data) {
    try {
        var objectID;
        if (exist(data))
            objectID = data.objectID;
        else
            objectID = "-1";
        var done = function(json){
            try {
                alfrescoFolderTabelle.clear();
                resizeTable("alfrescoCenterCenterNorth", "dtable3", "alfrescoFolderTabelle", "alfrescoFolderTabelleHeader", "alfrescoFolderTableFooter");
                alfrescoFolderTabelle.rows.add(json.data).draw();
                $.fn.dataTable.makeEditable(alfrescoFolderTabelle, updateInLineFolderFieldFieldDefinition());
                fillBreadCrumb(data);
                //$("#tree").jstree(true).refresh_node(objectID);
                var tree = $.jstree.reference('#tree');
                // Knoten einfügen
                for (var i = 0; i < json.data.length; i++){
                    if (!exist(tree.get_node(json.data[i].objectID)))
                        tree.create_node(tree.get_node(objectID), json.data[i] );
                }
                tree.select_node(objectID, true, false);
                tree.open_node(objectID);
            } catch (e) {
                errorHandler(e);
            }
        };
        // var json = executeService({"name": "listFolder", "callback": done, "errorMessage": "Verzeichnis konnte nicht aus dem Server gelesen werden:"}, [
        //     {"name": "folderId", "value": objectID},
        //     {"name": "withFolder", "value": -1}
        // ]);

            var len = calculateTableHeight("alfrescoCenterCenterCenter", "dtable2", alfrescoTabelle, "alfrescoTabelleHeader", "alfrescoTableFooter");
            alfrescoTabelle.page.len(len);
            alfrescoTabelle.settings().init().folderId = objectID;
            alfrescoTabelle.ajax.reload();
            $.fn.dataTable.makeEditable(alfrescoTabelle, updateInLineDocumentFieldDefinition());

        len = calculateTableHeight("alfrescoCenterCenterNorth", "dtable3", alfrescoFolderTabelle, "alfrescoFolderTabelleHeader", "alfrescoFOlderTableFooter")
        alfrescoFolderTabelle.page.len(len);
        alfrescoFolderTabelle.settings().init().folderId = objectID;
        alfrescoFolderTabelle.ajax.reload();
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * startet eine Suche im Alfresco Repository
 * @param searchText    der zu suchende Text
 */
function startSearch(searchText) {
    try {
        var sql = "select d.*, o.*, c.*, i.* from my:archivContent as d " +
            "join cm:titled as o on d.cmis:objectId = o.cmis:objectId " +
            "join my:amountable as c on d.cmis:objectId = c.cmis:objectId " +
            "join my:idable as i on d.cmis:objectId = i.cmis:objectId  WHERE IN_TREE(d, '" + archivFolderId + "') AND ( CONTAINS(d, 'cmis:name:*" + searchText + "* OR TEXT:" + searchText + "') OR CONTAINS(o, 'cm:title:*" + searchText + "*'))";
        var len = calculateTableHeight("searchCenter", "dtable4", alfrescoSearchTabelle, "alfrescoSearchTabelleHeader", "alfrescoSearchTableFooter");
        alfrescoSearchTabelle.page.len(len);
        alfrescoSearchTabelle.settings().init().cmisQuery = sql;
        alfrescoSearchTabelle.ajax.reload();
        $.fn.dataTable.makeEditable(alfrescoSearchTabelle, updateInLineDocumentFieldDefinition());
    } catch (e) {
        errorHandler(e);
    }
}


/**
 * behandelt die Clicks auf die Icons in der AlfrescoFoldertabelle
 */
function handleAlfrescoFolderImageClicks() {
    $(document).on("click", ".folderSwitch", function () {
        try {
             var tr = $(this).closest('tr');
             var row = alfrescoFolderTabelle.row( tr).data();
             $("#tree").jstree('deselect_all', true);
             switchAlfrescoDirectory(row);
        } catch (e) {
            errorHandler(e);
        }
     });
    $(document).on("click", ".folderCreate", function () {
        try {
            var tr = $(this).closest('tr');
            startFolderDialog(alfrescoFolderTabelle.row(tr).data(), "web-create", true);
        } catch (e) {
            errorHandler(e);
        }
    });
    $(document).on("click", ".folderRemove", function () {
        try {
            var tr = $(this).closest('tr');
            startFolderDialog(alfrescoFolderTabelle.row(tr).data(), "web-display", true);
        } catch (e) {
            errorHandler(e);
        }
    });
    $(document).on("click", ".folderEdit", function () {
        try {
            var tr = $(this).closest('tr');
            startFolderDialog(alfrescoFolderTabelle.row(tr).data(), "web-edit", true);
        } catch (e) {
            errorHandler(e);
        }
    });
}

/**
 * behandelt die Clicks auf die Icons in der Alfrescotabelle
 */
function handleAlfrescoImageClicks() {
    // Details bearbeiten
    $(document).on("click", ".detailEdit", function () {
        try {
            var tr = $(this).closest('tr');
            startDocumentDialog($('#' + tr[0].parentElement.parentElement.id).DataTable().row(tr).data(), "web-edit", true);
        } catch (e) {
            errorHandler(e);
        }
    });
    // Kommentare lesen
    $(document).on("click", ".showComments", function () {
        try {
            var tr = $(this).closest('tr');
            // Kommentare lesen
            var json = executeService({"name": "getComments", "errorMessage": "Kommentare konnten nicht gelesen werden!"}, [
                {
                    "name": "documentId",
                    "value": $('#' + tr[0].parentElement.parentElement.id).DataTable().row(tr).data().objectID
                }
            ]);
            if (json.success) {
                startCommentsDialog(json.data);
            }

        } catch (e) {
            errorHandler(e);
        }
    });
    // Dokument verschieben
    $(document).on("click", ".moveDocument", function () {
        try {
            var tr = $(this).closest('tr');
            var table = $('#' + tr[0].parentElement.parentElement.id).DataTable();
            startMoveDialog([table.row(tr)]);
        } catch (e) {
            errorHandler(e);
        }
    });
    // Dokument löschen
    $(document).on("click", ".deleteDocument", function () {
        try {
            var tr = $(this).closest('tr');
            startDocumentDialog($('#' + tr[0].parentElement.parentElement.id).DataTable().row(tr).data(), "web-display", true);
        } catch (e) {
            errorHandler(e);
        }
    });
    // Regeln
    $(document).on("click", ".rulesDocument", function () {
        try {
            var tr = $(this).closest('tr');
            var tabelle = $('#' + tr[0].parentElement.parentElement.id).DataTable();
            var id = tabelle.row(tr).data().objectID;
            var json = executeService({"name": "getDocumentContent", "errorMessage": "Dokument konnten nicht gelesen werden!"}, [
                {"name": "documentId", "value": tabelle.row(tr).data().objectID},
                {"name": "extract", "value": "true"}
            ]);
            if (json.success) {
                loadText(json.data, json.data, tabelle.row(tr).data().name, tabelle.row(tr).data().contentStreamMimeType, null);
                tabLayout.tabs("option", "active", 2);
            }
        } catch (e) {
            errorHandler(e);
        }

    });
    // Ziel im Ordner suchen
    $(document).on("click", ".detailAim", function () {
        try {
            var results = [];
            var tree = $('#tree').jstree(true);
            var tr = $(this).closest('tr');
            var tabelle = $('#' + tr[0].parentElement.parentElement.id).DataTable();
            var data = tabelle.row(tr).data();
            var id = data.objectID;
            if (data && data.parents) {
                var node = tree.get_node(data.parents[0].objectId);
                while (!node) {
                    var json = executeService({"name": "getNodeById", "errorMessage": "Dokument konnten nicht gelesen werden!"}, [
                        {"name": "documentId", "value": data.parents[0].objectId}
                    ]);
                    if (json.success) {
                        data = json.data;
                        results.push(data);
                        if (data && data.parents )
                            node = tree.get_node(data.parents[0].objectId);
                    } else {
                        break;
                    }
                }
            }
            if (node) {
                if (!results.length) {
                    tree.deselect_all(true);
                    tree.select_node(node, true);
                    tree.open_node(node, function(){
                        switchAlfrescoDirectory(node.data);
                        var row = alfrescoTabelle.row('#' + id);
                        if (row && row.length) {
                            row.draw().show().draw(false);
                            row.select();
                        }    
                    });
            
                }
                else {
                    results.push(node.data);
                    results.reverse();
                    // Hier muss mit einem Deferred Object gearbeitet werden, denn der open im Tree
                    // bewirkt einen asynchronen Aufruf, so das die nachfolgenden Operationen sonst nicht
                    // die notwendigen Daten haben.
                    var deffereds = $.Deferred(function (def) {
                        def.resolve();
                    });

                    for(var index = 0; index < results.length; index++) {
                        deffereds = (function(name, last, id, deferreds) {
                            return deferreds.then(function () {
                                return $.Deferred(function(def) {
                                    var node = tree.get_node(name.objectID);
                                    tree.open_node(node, function (last) {
                                        def.resolve();
                                        if (last){
                          
                                            node = tree.get_node(results[results.length - 1].objectID);
                                            tree.deselect_all(true);
                                            tree.select_node(node, true);
                                            tree.open_node(node, function(){
                                                switchAlfrescoDirectory(node.data);
                                                var row = alfrescoTabelle.row('#' + id);
                                                if (row && row.length) {
                                                    row.draw().show().draw(false);
                                                    row.select();
                                                }
                                            });
                                        }
                                           
                                    });
                                });
                            });
                        })(results[index], index == results.length -1, id, deffereds);
                    }
       
                }
                tabLayout.tabs("option", "active", 0);
            }

        } catch (e) {
            errorHandler(e);
        }
    });
}

/**
 * behandelt die Clicks auf die Icons in der Verteilungstabelle
 */
function handleVerteilungImageClicks() {
    $(document).on("click", ".run", function () {
        try {
            var tr = $(this).closest('tr');
            var row = tabelle.row(tr).data();
            var name = row[1];
            REC.currentDocument.setContent(daten[name]["text"]);
            REC.testRules(Verteilung.rulesEditor.getSession().getValue());
            daten[name].log = REC.mess;
            daten[name].result = results;
            daten[name].position = REC.positions;
            daten[name].xml = REC.currXMLName;
            daten[name].error = REC.errors;
            var ergebnis = [];
            ergebnis["error"] = REC.errors.length > 0;
            row[2] = REC.currXMLName.join(" : ");
            row[3] = ergebnis;
            row[5] = REC.errors;
            if (tabelle.fnUpdate(row, aPos[0]) > 0)
                message("Fehler", "Tabelle konnte nicht aktualisiert werden!");
        }
        catch (e) {
            errorHandler(e);
        }
    });
    $(document).on("click", ".glass", function () {
        try {
            var tr = $(this).closest('tr');
            var row = tabelle.row(tr).data();
            var name = row[1];
            multiMode = false;
            showMulti = true;
            currentFile = daten[name]["file"];
            document.getElementById('headerWest').textContent = currentFile;
            setXMLPosition(daten[name]["xml"]);
            Verteilung.textEditor.getSession().setValue(daten[name]["text"]);
            Verteilung.propsEditor.getSession().setValue(printResults(daten[name]["result"]));
            Verteilung.positions.setMarkers();
            //TODO das muss anders gemacht werden
            fillMessageBox(daten[name]["log"], true);
            manageControls();

        } catch (e) {
            errorHandler(e);
        }
    });
    $(document).on("click", ".loeschen", function () {
        try {
            var answer = confirm("Eintrag löschen?");
            if (answer) {
                var tr = $(this).closest('tr');
                var row = tabelle.row(tr).data();
                var name = row[1];
                try {
                    netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
                } catch (e) {
                    message("Fehler", "Permission to delete file was denied.");
                }
                currentFile = daten[name]["file"];
                Verteilung.textEditor.getSession().setValue("");
                Verteilung.propsEditor.getSession().setValue("");
                clearMessageBox();
                Verteilung.rulesEditor.getSession().foldAll(1);
                if (currentFile.length > 0) {
                    var file = Components.classes["@mozilla.org/file/local;1"].createInstance(Components.interfaces.nsILocalFile);
                    file.initWithPath(currentFile);
                    if (file.exists() == true)
                        file.remove(false);
                }
                tabelle.fnDeleteRow(aPos[0]);
            }
        } catch (e) {
            errorHandler(e);
        }
    });
    $(document).on("click", ".pdf", function (name) {
        try {
            var tr = $(this).closest('tr');
            var row = tabelle.row(tr).data();
            var name = row[1];
            var erg = executeService({"name": "openPDF", "errorMessage": "Dokument konnte nicht geöffnet werden!"}, [
                {"name": "fileName", "value": daten[name].file}
            ]);
            if (erg.success) {
                var file = new Blob([base64DecToArr(erg.data)], {type: erg.mimeType});
                var fileURL = URL.createObjectURL(file);
                window.open(fileURL);
            }
        } catch (e) {
            errorHandler(e);
        }
    });
    $(document).on("click", ".moveToInbox", function () {
        try {
            var tr = $(this).closest('tr');
            var row = tabelle.row(tr).data();
            var name = row[1];
            var docId = "workspace:/SpacesStore/" + daten[name]["container"];
            var json = executeService({"name": "createDocument", "errorMessage": "Dokument konnte nicht auf den Server geladen werden:", "successMessage": "Dokument " + name + " wurde erfolgreich in die Inbox verschoben!"}, [
                {"name": "documentId", "value": inboxFolderId},
                { "name": "fileName", "value": name},
                { "name": "documentContent", "value": daten[name].content, "type": "byte"},
                { "name": "documentType", "value": "application/pdf"},
                { "name": "extraCMSProperties", "value": ""},
                { "name": "versionState", "value": "major"}
            ]);
        } catch (e) {
            errorHandler(e);
        }
    });
}

/**
 * baut ein Tree kompatibles Objekt aus den übergebenen Daten auf
 * @param data    das übergebene Objekt
 * @return {{}}   das Tree kompatible Objekt
 */
function buildObjectForTree(data) {
    var item = {};
    if (data.baseTypeId == "cmis:folder") {
        // Eintrag ist vom Typ Folder
        item["icon"] = "";
        item["state"] = {"opened": false, "disabled": false, "selected": false};
        // Typen definieren
        item["type"] = "documentFolderStandard";
        if (data.objectID == alfrescoRootFolderId) {
            // Alfresco Root Folder
            item["type"] = "alfrescoRootFolderStandard";
        }
        if (data.objectID == archivFolderId) {
            // Archiv Folder
            item["type"] = "archivRootStandard";
        }
        if (data.objectID == inboxFolderId ||
            data.objectID == unknownFolderId) {
            // Die Standard Folder
            item["type"] = "archivFolderStandard";
        }
        if (data.objectID == fehlerFolderId) {
            // Fehler Folder
            item["type"] = "archivFehlerFolderStandard";
        }
        if (data.objectID == doubleFolderId) {
            // Fehler Folder
            item["type"] = "archivDoubleFolderStandard";
        }
        if (data.objectID == documentFolderId) {
            // Fehler Folder
            item["type"] = "archivDocumentFolderStandard";
        }
    } else {
        // Eintrag ist vom Typ Document
        item["icon"] = "";
        item["state"] = "";
    }
    item["id"] = data.objectID;
    item["children"] = data.hasChildFolder;
    item["text"] = data.name;
    item["data"] = data;
    item["a_attr"] = "'class': 'drop'";
    return item;
}


/**
 * Funktion wird beim Knotenwechsel aufgerufen.
 * Entweder ist ein Knoten angeben und der Inhalt des dazugehörigen Folder wird gelesen,
 * oder falls nicht dann wird der Inhalt des Root Folders gelesen.
 * Die Gefundenen Objekte (also die entsprechenden Subfolder) werden in jstree kompatible JSON Obekte konvertiert
 * @param aNode      der ausgewählte Knoten
 * @param callBack   der Callback, der die Werte in den Tree einträgt
 * @return
 */
function loadAndConvertDataForTree(aNode, callBack) {
    var obj = {};

    try {
        // keine Parameter mit gegeben, also den Rooteintrag erzeugen
        if (!exist(aNode)) {
            return [
                {
                    "icon": "",
                    "id": archivFolderId,
                    "text": "Archiv",
                    "state": {"opened": false, "disabled": false, "selected": false},
                    "children": true,
                    "type": "archivRootStandard"
                }
            ]
        } else {
            if (alfrescoServerAvailable) {
                // den Folder einlesen
                var done = function (json) {
                    if (json.success) {
                        obj = [];
                        for (var index = 0; index < json.data.length; index++) {
                            obj.push(buildObjectForTree(json.data[index]));
                        }
                        if (aNode.id == "#") {
                            obj = [
                                {
                                    "icon": "",
                                    "id": archivFolderId,
                                    "text": "Archiv",
                                    "state": {"opened": true, "disabled": false, "selected": true},
                                    "children": obj,
                                    "type": "archivRootStandard"
                                }
                            ];
                            // Daten für den ArchivRoot Ordner eintragen
                            if (REC.exist(json.data[0].parents[0]))
                                obj[0].data = json.data[0].parents[0];
                        }
                        // CallBack ausführen
                        if (obj) {
                            callBack.call(this, obj);
                        }
                    }
                    else {
                        message("Fehler", "Folder konnte nicht erfolgreich im Alfresco gelesen werden!");
                    }    
                };
                var json = executeService({"name": "listFolder", "callback": done, "errorMessage": "Verzeichnis konnte nicht aus dem Server gelesen werden:"}, [
                    {"name": "folderId", "value": aNode.id != "#" ? aNode.id : archivFolderId},
                    {"name": "withFolder", "value": -1}
                ]);
            }
        }
    } catch (e) {
        errorHandler(e);
    }
}


/**
 * lädt den Alfresco Tree
 */
function loadAlfrescoTree() {

    /**
     * prüft, ob das gedragte Objekt auf das Ziel Objekt gezogen werden kann
     * @param data       die Daten des Events
     * @return {boolean} true, der Knoten ist gültig
     */
    function checkMove(data) {
        var sourceData, targetData;
        var erg = false;
        var t = $(data.event.target);
        // prüfen, ob das Element entweder auf eine passende Tabellenzeile (treeDropable)
        // oder auf einen Tree Knoten (jstree-anchor) gezogen werden soll.
        if (t.closest('.treeDropable').length || t.closest('.jstree-anchor').length) {
            for(var index = 0; index < data.data.nodes.length; ++index) {
                erg = false;
                if (data.data.jstree) {
                    // Quelle ist der Tree
                    sourceData = $.jstree.reference('#tree').get_node(data.data.nodes[index]).data;
                } else {
                    // Quelle ist die Tabelle
                    // die Daten aus der Tabellenzeile, also der Folder, in den verschoben werden soll
                    if (data.data.table == "alfrescoFolderTabelle") {
                        sourceData = alfrescoFolderTabelle.row('#' + data.data.nodes[index]).data();
                    } else if (data.data.table == "alfrescoTabelle") {
                        sourceData = alfrescoTabelle.row('#' + data.data.nodes[index]).data();
                    }
                }
                if (data.event.target.className.indexOf("jstree-anchor") != -1) {
                    // Ziel ist der Tree
                    // der Zielknoten
                    targetData = $.jstree.reference('#tree').get_node(t).data;
                } else if (data.event.target.className.indexOf("treeDropable") != -1 && data.event.target.className.indexOf("alfrescoTableDragable") == -1) {
                    // Ziel ist die Tabelle
                    // die Ziel Zeile
                    var row = alfrescoFolderTabelle.row(t.closest('.treeDropable')[0].parentElement);
                    if (row && row.length)
                        targetData = row.data();
                }
                // Object darf nicht in die Standard Ordner geschoben werden und die Standard Ordner dürfen generell nicht verschoben werden
                if (sourceData &&
                        // der zu verschiebene Ordner darf nicht der ArchivRootFolder sein
                    sourceData.objectID != archivFolderId &&
                        // der zu verschiebene Ordner darf nicht der Dokumenten Folder sein
                    sourceData.objectID != documentFolderId &&
                        // der zu verschiebene Ordner darf nicht derFehler Folder sein
                    sourceData.objectID != fehlerFolderId &&
                        // der zu verschiebene Ordner darf nicht der Inbox Folder sein
                    sourceData.objectID != inboxFolderId &&
                        // der zu verschiebene Ordner darf nicht der Unknown Folder sein
                    sourceData.objectID != unknownFolderId &&
                        // der zu verschiebene Ordner darf nicht der Doppelte Folder sein
                    sourceData.objectID != doubleFolderId &&
                    targetData &&
                        // Dokumente dürfen nicht direkt in den Dokumenten Ordner
                    (sourceData.baseTypeId == "cmis:folder" || (sourceData.baseTypeId == "cmis:document" && targetData.objectID != documentFolderId)) &&
                        // Ziel für Objekte darf nicht AlfrescoRootFolder sein
                    targetData.objectID != alfrescoRootFolderId &&
                        // Ziel für den Objekte darf nicht der ArchivRootFolder sein
                    targetData.objectID != archivFolderId &&
                        // Ziel für Ordner darf nicht Fehler Folder sein
                    (sourceData.baseTypeId == "cmis:document" || (sourceData.baseTypeId == "cmis:folder" && targetData.objectID != fehlerFolderId)) &&
                        // Ziel für den Ordner darf nicht der Folder für die Doppelten sein
                    (sourceData.baseTypeId == "cmis:document" || (sourceData.baseTypeId == "cmis:folder" && targetData.objectID != doubleFolderId)) &&
                        // Ziel für den Ordner darf nicht  Unbekannten Folder sein
                    (sourceData.baseTypeId == "cmis:document" || (sourceData.baseTypeId == "cmis:folder" && targetData.objectID != unknownFolderId)) &&
                        // Ziel für den Ordner darf nicht der Inbox Folder sein
                    (sourceData.baseTypeId == "cmis:document" || (sourceData.baseTypeId == "cmis:folder" && targetData.objectID != inboxFolderId)) &&
                        // Ziel für den Ordner darf nicht Parent Folder sein, da ist er nämlich schon drin
                    targetData.objectID != sourceData.parentId &&
                        // Ziel für den Ordner darf nicht derselbe oder ein eigener Child Folder sein, denn sonst würde der Knoten "entwurzelt"
                    getAllChildrenIds($.jstree.reference('#tree'), sourceData.objectID).indexOf(targetData.objectID) == -1) {
                    erg = true;
                }
                if (!erg)
                    break;
            }
        }
        return erg;
    }

    /**
     * sammelt alle Ids des Knoten und seiner Children
     * die Methode dient zum Prüfen ob ein Knoten in einen anderen verschoben werden kann.
     * Das geht nämlich nicht wenn der Ziel Knoten ein Children des zu verschiebenden Knotens ist
     * @param treeObj        die Referenz auf den Tree
     * @param nodeId         die Id des zu verschiebenden Knoten
     * @return {Array}       ein Array mit den Ids der Childknoten
     */
    function getAllChildrenIds(treeObj, nodeId) {
        var result = [];
        var node = treeObj.get_node(nodeId);
        result.push(node.id);
        if (node.children) {
            for(var i = 0; i < node.children.length; i++) {
                result = result.concat(getAllChildrenIds(treeObj, node.children[i]));
            }
        }
        return result;
    }

    function customMenu(node) {
        var tree = $("#tree").jstree(true);
        var items = {
            "create": {
                "separator_before": false,
                "separator_after": false,
                "label": "Erstellen",
                "icon" : "./images/details_open.png",
                "action": function (obj) {
                    try {
                        startFolderDialog($.jstree.reference('#tree').get_node(obj.reference[0]).data, "web-create", true);
                    } catch (e) {
                        errorHandler(e);
                    }
                }
            },
            "rename": {
                "separator_before": false,
                "separator_after": false,
                "label": "Ändern",
                "icon" : "./images/beautify16.png",
                "action": function (obj) {
                    try {
                        startFolderDialog($.jstree.reference('#tree').get_node(obj.reference[0]).data, "web-edit", true);
                    } catch (e) {
                        errorHandler(e);
                    }
                }
            },
            "delete": {
                "separator_before": false,
                "separator_after": false,
                "label": "Löschen",
                "icon" : "./images/deleteDocument.gif",
                "action": function (obj) {
                    try {
                        startFolderDialog($.jstree.reference('#tree').get_node(obj.reference[0]).data, "web-display", true);
                    } catch (e) {
                        errorHandler(e);
                    }
                }
            }
        };
        // Archivroot hat kein Kontextmenü
        if (tree.get_type(node) == "archivRootStandard")
            return false;
        // Im Fehlerordner kein Delete und Create
        if (tree.get_type(node) == "archivFehlerFolderStandard") {
            delete items.delete;
            delete items.create;
        }
        // In Standardordnern kein Delete und Create
        if (tree.get_type(node) == "archivFolderStandard") {
            delete items.delete;
            delete items.create;
        }
        // Im Ordner für die Dokumente kein Delete
        if (tree.get_type(node) == "archivDocumentFolderStandard") {
            delete items.delete;
        }
        return items;
    }


    try {
        $("#tree").jstree('destroy');
    } catch (e) {
    }

    /*
    Aufbau des Tree's
     */
    try {
        tree = $("#tree").jstree({
            'core': {
                'data': function (node, aFunction) {
                    try {
                        // relevante Knoten im Alfresco suchen
                        loadAndConvertDataForTree(node, aFunction);
                    } catch (e) {
                        errorHandler(e);
                    }
                },
                error : function (err) {  
                    REC.log(DEBUG, err.reason);
                    fillMessageBox(true);
                },
                'check_callback': function (op, node, par, pos, more) {
                    try {
                        var erg = false;
                        // Umbenannt werden darf alles
                        if (op === "rename_node")
                            return true;
                        // Keine Verzeichnisse in die Archiv Standardordner verschieben (ausser Ordner Dokumente)
                        if ((op === "move_node" ||
                             op === "copy_node" ||
                             op === "create_node" ||
                             op === "delete_node") &&
                             node.data &&
                             node.data.objectID != alfrescoRootFolderId &&
                             node.data.objectID != archivFolderId &&
                             node.data.objectID != inboxFolderId &&
                             node.data.objectID != fehlerFolderId &&
                             node.data.objectID != unknownFolderId &&
                             node.data.objectID != doubleFolderId &&
                             node.data.objectID != documentFolderId &&
                             node.data.baseTypeId == "cmis:folder" &&
                             par &&
                             par.id &&
                             par.id != alfrescoRootFolderId &&
                             par.id != archivFolderId &&
                             par.id != inboxFolderId &&
                             par.id != fehlerFolderId &&
                             par.id != unknownFolderId &&
                             par.id != doubleFolderId) {
                            erg = true;
                        }
                        // Dokumente nicht in die Root Verzeichenisse Archiv und Dokumente und nicht in das Verzeichnis wo es gerade ist.
                        if ((op === "move_node" ||
                             op === "copy_node") &&
                             node.data &&
                             node.data.baseTypeId == "cmis:document" &&
                             par &&
                             par.id &&
                             par.id != alfrescoRootFolderId &&
                             par.id != archivFolderId &&
                             par.id != documentFolderId &&
                             par.id != node.data.parentId ) {
                            erg = true;
                        }
                        return erg;
                    } catch (e) {
                        errorHandler(e);
                    }
                },
                'themes': {
                    'responsive': true,
                    'variant': 'big',
                    'stripes': false,
                    'dots': true,
                    'icons': true
                }
            },
            'types' : {
                '#' : {
                    "max_children" : 1
                },
                'archivRootStandard' : {
                    "valid_children" : ["archivFolderStandard", "archivDocumentFolderStandard", "archivFehlerFolderStandard"],
                    "icon": "fa fa-file-text-o fa-15x awesomeEntity"
                },
                'archivFolderStandard' : {
                    "valid_children" : [],
                    "icon": "fa fa-file-text-o fa-15x awesomeEntity"
                },
                'archivDoubleFolderStandard' : {
                    "valid_children" : [],
                    "icon": "fa fa-file-text-o fa-15x awesomeEntity"
                },
                'archivFehlerFolderStandard' : {
                    "valid_children" : ["archivDoubleFolderStandard"],
                    "icon": "fa fa-file-text-o fa-15x awesomeEntity"
                },
                'archivDocumentFolderStandard' : {
                    "valid_children" : ["documentFolderStandard"] ,
                    "icon": "fa fa-file-text-o fa-15x awesomeEntity"
                },
                'documentFolderStandard' : {
                    "valid_children" : -1,
                    "icon": "fa fa-file-text-o fa-15x awesomeEntity"
                }
            },
            "contextmenu": {
                "items": customMenu,
                "select_node" : false
            },
            'plugins': ["dnd", "types", "contextmenu"]
        }).on("changed.jstree",  function (event, data){
            try {
                if (data.action == "select_node") {
                    var tree = $("#tree").jstree(true);
                    var evt = window.event || event;
                    var button = evt.which || evt.button;

                    // Select Node nicht bei rechter Maustaste
                    if (button != 1 && ( typeof button != "undefined")) {
                        return false;
                    }
                    if (!data.node || !data.node.data)
                    // für den Root Node
                        switchAlfrescoDirectory(null);
                    else {
                        if (data.node.data.baseTypeId == "cmis:folder") {
                            if (alfrescoServerAvailable) {
                                switchAlfrescoDirectory(data.node.data);
                                tree.open_node(data.node.id);
                            }
                        }
                    }
                }
            } catch (e) {
                errorHandler(e);
            }
        }).on("load_node.jstree", function (event, data) {
            try {
                if (data.node.id == inboxFolderId ) {
                    // Eventlistner für Drop in Inbox
                    var zone = document.getElementById(inboxFolderId);
                    zone.addEventListener('dragover', handleDragOver, false);
                    zone.addEventListener('drop', handleDropInbox, false);
                }
            } catch (e) {
                errorHandler(e);
            }
        }).on('move_node.jstree', function (event, data) {
            // Knoten innerhalb des Trees per Drag and Drop verschieben
            try {
                var nodeId = data.node.data.objectID;
                var parentId = data.node.data.parentId;
                var destinationId = data.parent;
                var done = function (json) {
                    if (json.success) {
                        var newData = json.data;
                        var source = json.source;
                        var target = json.target;
                        REC.log(INFORMATIONAL, "Ordner " + data.node.data.name + " von " + source.path + " nach " + target.path + " verschoben");
                        fillMessageBox(true);
                        // Bei Bedarf den Ordner aus der Tabelle entfernen
                        var row = alfrescoFolderTabelle.row('#' + nodeId);
                        if (row && row.length) {
                            row.remove();
                            alfrescoFolderTabelle.draw();
                        }
                        // Bei Bedarf den neuen Ordner in die Tabelle einfügen
                        if (jQuery("#breadcrumblist").children().last().get(0).id == newData.parentId)
                            alfrescoFolderTabelle.rows.add([newData]).draw();
                        // Das Objekt im Tree mit dem geänderten Knoten aktualisieren
                        data.node.data = newData;
                    }
                };
                executeService({"name": "moveNode", "callback": done, "errorMessage": "Ordner konnte nicht verschoben werden:"}, [
                    {"name": "documentId", "value": nodeId},
                    {"name": "currentLocationId", "value": parentId},
                    {"name": "destinationId", "value": destinationId}
                ]);
            } catch (e) {
                errorHandler(e);
            }
        }).on('loaded.jstree', function (event, data) {
            try {
                loadAlfrescoTable();
                loadAlfrescoFolderTable();
            } catch (e) {
                errorHandler(e);
            }
        });

        // Drag and Drop für Verschieben von Ordnern aus dem Tree in die Alfresco Folder Tabelle
        $(document)
            .on('dnd_move.vakata', function (e, data) {
                // Hier wird geprüft ob der Ordner, auf den das Element gezogen werden soll ein zulässiger Ordner ist
                // es wird hier aber nicht verhindert, dass das verschieben trotzdem geht sondern nur das Icon beim gezogenen
                // Objekt gesetzt.
                try {
                    var erg = checkMove(data);
                    if (erg) {
                        // Das Icon an dem gezogenen Objekt auf Ok setzen
                        data.helper.find('.jstree-icon').removeClass('jstree-er').addClass('jstree-ok');
                    } else {
                        // Das Icon an dem gezogenen Objekt auf Error setzen
                        data.helper.find('.jstree-icon').removeClass('jstree-ok').addClass('jstree-er');
                    }
                } catch (e) {
                    errorHandler(e);
                }
            }).on('dnd_stop.vakata', function (e, data) {
            // das eigentliche verschieben per Drag and Drop
            try {
                // nochmal die Zulässigkeit des Drop prüfen!!
                if (checkMove(data)) {
                    var sourceData, targetData;
                    var t = $(data.event.target);
                    if (t.closest('.treeDropable').length || t.closest('.jstree-anchor').length) {

                        for(var index = 0; index < data.data.nodes.length; ++index) {

                            if (data.data.jstree) {
                                // Quelle ist der Tree
                                sourceData = $.jstree.reference('#tree').get_node(data.data.nodes[index]).data;
                            } else {
                                // Quelle ist die Tabelle
                                // die Daten aus der Tabellenzeile, also der Folder, in den verschoben werden soll
                                if (data.data.table == "alfrescoFolderTabelle") {
                                    sourceData = alfrescoFolderTabelle.row('#' + data.data.nodes[index]).data();
                                } else if (data.data.table == "alfrescoTabelle") {
                                    var row = alfrescoTabelle.row('#' + data.data.nodes[index]);
                                    if (row && row.length)
                                        sourceData = row.data();
                                }
                            }
                            if (data.event.target.className.indexOf("jstree-anchor") != -1) {
                                // Ziel ist der Tree
                                // der Zielknoten
                                targetData = $.jstree.reference('#tree').get_node(t).data;
                            } else if (data.event.target.className.indexOf("treeDropable") != -1) {
                                // Ziel ist die Tabelle
                                // die Ziel Zeile
                                targetData = alfrescoFolderTabelle.row(t.closest('.treeDropable')[0].parentElement).data();
                            }
                            if (sourceData.baseTypeId == "cmis:folder") {
                                // Tree updaten
                                var sourceNode = $.jstree.reference('#tree').get_node(sourceData.objectID);
                                var targetNode = $.jstree.reference('#tree').get_node(targetData.objectID);
                                if (sourceNode && targetNode)
                                    $.jstree.reference('#tree').move_node(sourceNode, targetNode);
                                // Der Rest passiert im move_node Handler!!
                            } else {
                                var done = function(json) {
                                    if (json.success) {
                                        var newData = json.data;
                                        var source = json.source;
                                        var target = json.target;
                                        REC.log(INFORMATIONAL, "Dokument " + sourceData.name + " von " + source.path + " nach " + target.path + " verschoben");
                                        fillMessageBox(true);
                                        row.remove();
                                        alfrescoTabelle.draw();
                                    }
                                };
                                //Dokument wurde verschoben
                                var json = executeService({"name": "moveNode", "callback": done, "errorMessage": "Dokument konnte nicht verschoben werden:"}, [
                                    {"name": "documentId", "value": sourceData.objectID},
                                    {"name": "currentLocationId", "value": sourceData.parentId},
                                    {"name": "destinationId", "value": targetData.objectID}
                                ]);

                            }
                        }
                    }
                }
            } catch (e) {
                errorHandler(e);
            }
        });


        // // Initiales Lesen
        // if (alfrescoServerAvailable)
        //     switchAlfrescoDirectory({objectID: archivFolderId});

    } catch (e) {
        errorHandler(e);
    }
}

/**
 * lädt die Buttons
 */
function loadButtons() {
    $("button:first").button({
        icons: {
            secondary: "ui-icon-triangle-1-s"
        },
        text: true
    })
        .click(function (event) {
            event.preventDefault();
            $( "#menu" ).menu();
        });
}


/**
 * prüft und baut das Alfresco Environment auf.
 * @return true, wenn alles geklappt hat
 * TODO Regel für die Inbox
 */
function checkAndBuidAlfrescoEnvironment() {

    /**
     * baut einen Alfresco Archiv Ordner auf
     * @param folder               der Pfad des Ordners (z.B. /Archiv/Fehler/Doppelte) Aufgebaut wird nur der letzte, also "Doppelte"
     * @param id                   die Id des Ordners in dem  der Ordner erstellt werden soll
     * @param txt                  die Beschreibung für den Ordner
     * @return {{result, success}| das Ergebnis}
     */
    function buildAlfrescoFolder(folder, id, txt) {
        var erg = executeService({"name": "getNodeId", "ignoreError": true}, [
            {"name": "filePath", "value": folder}
        ]);
        if (!erg.success) {
            var name = folder.split("/").pop();
            var extraProperties = {"cmis:folder": {"cmis:name": "" + name +""}, "P:cm:titled":{"cm:title": "" + name +"", "cm:description":"" + txt + ""}};
            erg = executeService({"name": "createFolder"}, [
                {"name": "documentId", "value": id},
                {"name": "extraProperties", "value": extraProperties}
            ]);
            if (erg.success) {
                erg = executeService({"name": "getNodeId", "errorMessage": txt + " konnte nicht gefunden werden:"}, [
                    {"name": "filePath", "value": folder}
                ]);
                if (!erg.success)
                    REC.log(WARN, txt + " konnte auf dem Alfresco Server nicht gefunden werden!");
            }
        }
        return erg;
    }

    
    var ret, erg;
    // ist schon ein Alfresco Server verbunden?
    erg = executeService({"name": "isConnected"});
    if (erg.success && erg.data) {
        alfrescoServerAvailable = true;
    }
    else {
        // prüfen, ob Server ansprechbar ist
        if (exist(getSettings("server")))
            alfrescoServerAvailable = checkServerStatus(getSettings("server"));
        // Ticket besorgen
        if (exist(getSettings("user")) && exist(getSettings("password")) && exist(getSettings("server"))) {
            //TODO
            erg = executeService({"name": "getTicketWithUserAndPassword"},
                [{"name": "user", "value": getSettings("user")},
                    {"name": "password", "value": getSettings("password")},
                    {"name": "server", "value": getSettings("server")}
                ]);
            if (erg.success) {
                // Binding prüfen
                if (alfrescoServerAvailable && exist(getSettings("binding")))
                if (checkServerStatus(getSettings("binding") + "?alf_ticket=" + erg.data)) {
                    erg = executeService({"name": "setParameter", "errorMessage" : "Parameter für die Services konnten nicht gesetzt werden:"}, [
                        {"name": "server", "value": getSettings("server")},
                        {"name": "binding", "value": getSettings("binding")},
                        {"name": "user", "value": getSettings("user")},
                        {"name": "password", "value": getSettings("password")}
                    ]);
                    if (!erg.success) {
                        REC.log(WARN, "Binding Parameter konnten nicht gesetzt werden!");
                    } else
                        alfrescoServerAvailable = false;    
                }
            } else {
                alfrescoServerAvailable = false;
            }
        } else {
            alfrescoServerAvailable = false;
        }
    }
    // falls ja, dann Server Parameter eintragen
    if (alfrescoServerAvailable) {
        var extraProperties;
        // Skript Verzeichnis prüfen
        erg = executeService({"name" : "getNodeId", "ignoreError" : true}, [
            {"name": "filePath", "value": "/Datenverzeichnis/Skripte"}
        ]);
        if (erg.success)
            scriptFolderId = erg.data;
        else {
            REC.log(WARN, "Verzeichnis '/Datenverzeichnis/Skripte' auf dem Alfresco Server nicht gefunden!");
        }
        // Verteilskript prüfen
        if (erg.success) {
            erg = executeService({"name" : "getNodeId", "ignoreError" : true}, [
                {"name": "filePath", "value": "/Datenverzeichnis/Skripte/recognition.js"}
            ]);
            if (!erg.success) {
                var script = $.ajax({
                    url: createPathToFile("./js/recognition.js"),
                    async: false
                }).responseText;

                if (exist(script) && script.length > 0) {
                    erg = executeService({
                        "name": "createDocument",
                        "errorMessage": "Verteilungsskript konnte nicht erstellt werden!"
                    }, [
                        {"name": "documentId", "value": scriptFolderId},
                        {"name": "fileName", "value": "recognition.js"},
                        {"name": "content", "value": base64EncArr(strToUTF8Arr(script))},
                        {"name": "mimeType", "value": "application/x-javascript"},
                        {
                            "name": "extraProperties",
                            "value":  {"cmis:document": {"cmis:name": "recognition.js"}, "P:cm:titled": {"cm:description": "Skript zum Verteilen der Dokumente" }}
                        },
                        {"name": "versionState", "value": "major"}
                    ]);
                     if (erg.success)
                        scriptID = erg.data.objectId;
                    else {
                        REC.log(WARN, "Verteilscript (recognition.js) konnte auf dem Alfresco Server nicht angelegt werden!");
                    }
                } else
                    REC.log(WARN, "Verteilscript (recognition.js) konnte nicht gelesen werden!");
            } else {
                scriptID = erg.data;
            }
        }
        if (erg.success) {
            // Regeln prüfen
            erg = executeService({"name" : "getNodeId", "ignoreError" : true}, [
                {"name": "filePath", "value": "/Datenverzeichnis/Skripte/doc.xml"}
            ]);
            if (!erg.success) {
                var doc = $.ajax({
                    url: createPathToFile("./rules/doc.xml"),
                    async: false
                }).responseText;
                if (exist(doc) && doc.length > 0) {
                    erg = executeService({
                        "name": "createDocument",
                        "errorMessage": "Verteilungsregeln konnten nicht erstellt werden!"
                    }, [
                        {"name": "documentId", "value": scriptFolderId},
                        {"name": "fileName", "value": "doc.xml"},
                        {"name": "content", "value": base64EncArr(strToUTF8Arr(doc))},
                        {"name": "mimeType", "value": "text/xml"},
                        {
                            "name": "extraProperties",
                            "value": {"cmis:document":{"cmis:name": "doc.xml"}, "P:cm:titled":{"cm:description":"Dokument mit den Verteil-Regeln"}}
                        },
                        {"name": "versionState", "value": "major"}

                    ]);
                    if (erg.success)
                        rulesID = erg.data.objectId;
                    else {
                        REC.log(WARN, "Verteilregeln (doc.xml) konnten auf dem Alfresco Server nicht angelegt werden!");
                    }
                } else
                    REC.log(WARN, "Verteilregeln (doc.xml) konnten nicht gelesen werden!");
            } else {
                rulesID = erg.data;
            }
        }
        if (erg.success) {
            // Schema prüfen
            erg = executeService({"name" : "getNodeId", "ignoreError" : true}, [
                {"name": "filePath", "value": "/Datenverzeichnis/Skripte/doc.xsd"}
            ]);
            if (!erg.success) {
                var doc = $.ajax({
                    url: createPathToFile("./rules/doc.xsd"),
                    async: false
                }).responseText;
                if (exist(doc) && doc.length > 0) {
                    erg = executeService({
                        "name": "createDocument",
                        "errorMessage": "Verteilungsschema konnten nicht erstellt werden!"
                    }, [
                        {"name": "documentId", "value": scriptFolderId},
                        {"name": "fileName", "value": "doc.xsd"},
                        {"name": "content", "value": base64EncArr(strToUTF8Arr(doc))},
                        {"name": "mimeType", "value": "text/xml"},
                        {
                            "name": "extraProperties",
                            "value": {"cmis:document":{"cmis:name": "doc.xsd"}, "P:cm:titled":{"cm:description":"Dokument mit den Verteilschema"}}
                        },
                        {"name": "versionState", "value": "major"}

                    ]);
                    if (erg.success)
                        rulesSchemaId = erg.data.objectId;
                    else {
                        REC.log(WARN, "Verteilschema (doc.xsd) konnten auf dem Alfresco Server nicht angelegt werden!");
                    }
                } else
                    REC.log(WARN, "Verteilschema (doc.xsd) konnten nicht gelesen werden!");
            } else {
                rulesSchemaId = erg.data;
            }
        }
        if (erg.success) {
            // Archiv prüfen
            erg = executeService({"name" : "getNodeId", "errorMessage" : "Archiv konnte nicht gefunden werden:"}, [
                {"name": "filePath", "value": "/"}
            ]);
            if (erg.success) {
                alfrescoRootFolderId = erg.data;
            } else {
                REC.log(WARN, "Root konnte auf dem Server nicht gefunden werden!");
            }
        }
        if (erg.success) {
            erg = buildAlfrescoFolder("/Archiv", alfrescoRootFolderId, "Der Archiv Root Ordner");
            if (erg.success)
                archivFolderId = erg.data;
            else
                REC.log(WARN, "Archiv konnte auf dem Alfresco Server nicht gefunden werden!");

        }
        if (erg.success) {
            // Archiv Root prüfen
            erg = buildAlfrescoFolder("/Archiv/Dokumente", archivFolderId, "Der Ordner für die abgelegten Dokumente");
            if (erg.success) {
                documentFolderId = erg.data;
            }
        }
        if (erg.success) {
            // Inbox prüfen
            erg = buildAlfrescoFolder("/Archiv/Inbox", archivFolderId, "Der Posteingangsordner");
            if (erg.success) {
                inboxFolderId = erg.data;
            }
        }
        if (erg.success) {
            // Fehlerbox prüfen
            erg = buildAlfrescoFolder("/Archiv/Fehler", archivFolderId, "Der Ordner für nicht verteilbare Dokumente");
            if (erg.success) {
                fehlerFolderId = erg.data;
            }
        }
        if (erg.success) {
            // Unbekanntbox prüfen
            erg = buildAlfrescoFolder("/Archiv/Unbekannt", archivFolderId, "Der Ordner für unbekannte Dokumente");
            if (erg.success) {
                unknownFolderId = erg.data;
            }
        }
        if (erg.success) {
            // Doppelte Box prüfen
            erg = buildAlfrescoFolder("/Archiv/Fehler/Doppelte", fehlerFolderId, "Verzeichnis für doppelte Dokumente");
            if (erg.success) {
                doubleFolderId = erg.data;
            }
        }
        
        if (erg.success) {

            tabLayout.tabs({
                disabled: [],
                active: 0
            });
            ret = erg.success;
        }
    } else {
        if (exist(getSettings("server")))
            REC.log(WARN, "Server " + getSettings("server") + " ist nicht erreichbar!");
        tabLayout.tabs({
            disabled: [0, 1],
            active: 2
        });
        ret = true;
    }
    return ret;
}



/**
 * initialisiert die Anwendung
 */
function initApplication() {
    var erg;
    try {
        erg = executeService({"name": "isConnected", "ignoreError": true});
        if (erg.success && erg.data) {
            erg = executeService({"name": "getConnection", "ignoreError": true});
            if (erg.success) {
                settings = {
                    "settings": [
                        {"key": "server", "value": erg.data.server},
                        {"key": "user", "value": erg.data.user},
                        {"key": "password", "value": erg.data.password},
                        {"key": "binding", "value": erg.data.binding}                    ]
                };
            }
        }
        // Settings schon vorhanden?
        if (!exist(getSettings("server")) || !exist(getSettings("binding")) || !exist(getSettings("user")) || !exist(getSettings("password"))) {
            var cookie = $.cookie("settings");
            // prüfen, ob ein Cookie vorhanden ist
            if (REC.exist(cookie)) {
                // Cookie ist vorhanden, also die Daten aus diesem verwenden
                settings = $.parseJSON(cookie);
            } else {
                settings = {settings:[]};
            }
        }
        checkAndBuidAlfrescoEnvironment();
        openRules();
        manageControls();
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * startet die Anwendung
 */
function start() {
    try {

        $(document).tooltip();
        $('.ui-dialog-titlebar-close').tooltip('disable');
        //$("#switcher").themeswitcher();
        jQuery("#breadcrumb").jBreadCrumb();
        loadLayout();
        document.getElementById('filesinput').addEventListener('change', readMultiFile, false);
        REC.init();
        Verteilung.propsEditor = ace.edit("inProps");
        Verteilung.propsEditor.setReadOnly(true);
        Verteilung.propsEditor.renderer.setShowGutter(false);
        Verteilung.propsEditor.setShowPrintMargin(false);
        Verteilung.propsEditor.$blockScrolling = Infinity;
        Verteilung.outputEditor = ace.edit("inOutput");
        Verteilung.outputEditor.setReadOnly(true);
        Verteilung.outputEditor.setShowPrintMargin(false);
        Verteilung.outputEditor.$blockScrolling = Infinity;
        var zoneRules = document.getElementById('inRules');
        zoneRules.addEventListener('dragover', handleDragOver, false);
        zoneRules.addEventListener('drop', handleRulesSelect, false);

        Verteilung.rulesEditor = ace.edit("inRules");
        Verteilung.rulesEditor.getSession().setMode("ace/mode/xml");
        Verteilung.rulesEditor.setShowPrintMargin(false);
        Verteilung.rulesEditor.setDisplayIndentGuides(true);
        Verteilung.rulesEditor.commands.addCommand({
            name: "save",
            bindKey: {
                win: "Ctrl-Shift-S",
                mac: "Command-s"
            },
            exec: save
        });
        Verteilung.rulesEditor.commands.addCommand({
            name: "format",
            bindKey: {
                win: "Ctrl-Shift-F",
                mac: "Command-f"
            },
            exec: format
        });
        Verteilung.rulesEditor.$blockScrolling = Infinity;
        Verteilung.textEditor = ace.edit("inTxt");
        Verteilung.textEditor.setTheme("ace/theme/chrome");
        Verteilung.textEditor.setShowInvisibles(true);
        Verteilung.textEditor.setShowPrintMargin(false);
        Verteilung.textEditor.getSession().setMode("ace/mode/text");
        Verteilung.textEditor.$blockScrolling = Infinity;
        var zone = document.getElementById('inTxt');
        zone.addEventListener('dragover', handleDragOver, false);
        zone.addEventListener('drop', handleFileSelect, false);
        // Zahlenformat festlegen
        $.format.locale({
            number: {
                groupingSeparator: '.',
                decimalSeparator: ','
            }
        });
        initApplication();
        loadAlfrescoTree();

        loadAlfrescoSearchTable();
        loadVerteilungTable();


        // Eventhandler für die Image Clicks
        handleVerteilungImageClicks();
        handleAlfrescoFolderImageClicks();
        handleAlfrescoImageClicks();

        //loadButtons();
        // Icon Buttons
        $("#alfrescoSearchButton").button({
            icons: {
                primary: 'ui-icon-search'
            }
        });

        $('#alfrescoSearch').on('keypress', function (event) {
            if(event.which === 13){
                startSearch($(this).val());
            }
        });

        viewMenuNormal = $('#menu-1').superfish();
        viewMenuSearch = $('#menu-2').superfish();
        fillMessageBox(true);
    } catch(e) {
        errorHandler(e);
    }
}

