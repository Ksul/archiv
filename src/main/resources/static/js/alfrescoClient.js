/**
 * Eventhandler der für die Verarbeitung von fallen gelassen Dateien auf die Inbox zuständig ist
 * @param evt  das Event
 */
function handleDropInbox(evt) {
    evt.stopPropagation();
    evt.preventDefault();
    const files = evt.dataTransfer.files;
    for ( let i = 0; i < files.length; i++) {
        let f = files[i];
        if (f) {
            const reader = new FileReader();
            reader.onloadend = (function (f) {
                return function (evt) {
                    if (evt.target.readyState === FileReader.DONE) {
                        const content = evt.target.result;
                        let json = executeService({"name": "createDocument", "errorMessage": "Dokument konnten nicht im Alfresco angelegt werden!"},[
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
            alertify.alert("Fehler", "Failed to load file!");
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
 * @return
 */
function calculateTableHeight(panel, divId, tabelle, headerId, footerId) {
    let div, rowHeight = 0, availableHeight = 0, topPanel = 0, downPanel = 0,columnPanel = 0, headerPanel = 0, footerPanel = 0;
    div = $('#'+divId);
    availableHeight = $('#'+panel).height();
    let children =  div.children().children();
    if (children.length > 0)
        topPanel = children[0].offsetHeight;
    if (children.length > 2)
        downPanel = children[2].offsetHeight;
    if (children.length > 1)
        columnPanel = children[0].offsetHeight;
    headerPanel = $('#'+headerId).height();
    footerPanel = $('#'+footerId).height();
    if (tabelle && tabelle.settings().init().iconView && tabelle.settings().init().iconView) // fest nach aktueller CSS
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

// /**
//  * setzt die Pagelength in der Tabelle
//  * @param panel         das Layoutpanel, welches die Tabelle enthält
//  * @param divId         die Id des DIV's welches die Tabelle enthält
//  * @param tabelleId     die Id der Tabelle
//  * @param headerId      die Id des headers
//  * @param footerId      die Id des Footers
//  */
// function resizeTable(panel, divId, tabelleId, headerId, footerId) {
//
//     const tabelle = $('#'+tabelleId).DataTable();
//     const drawRows = calculateTableHeight(panel, divId, tabelle, headerId, footerId);
//
//     if ( drawRows !== Infinity && drawRows !== -Infinity &&
//         ! isNaN( drawRows )   && drawRows > 0 &&
//         drawRows !== tabelle.page.len()
//     ) {
//         tabelle.page.len( drawRows ).draw("page");
//     }
// }


// function asumeCountOfTableEntries(panel,  divId, tabelleId,headerId, footerId) {
//     const div = $('#'+divId);
//     const completePanel = $('#' + panel).height();
//     const topPanel = div.children().children()[0].offsetHeight;
//     const downPanel = div.children().children()[2].offsetHeight;
//     const columnPanel = div.children().children()[1].children[0].offsetHeight;
//     const headerPanel = $('#' + headerId).height();
//     const footerPanel = $('#' + footerId).height();
//     const rowHeight = $('.odd') + 2;
//     return Math.floor((completePanel - topPanel - headerPanel - columnPanel - downPanel - footerPanel) / rowHeight);
// }

/**
 * 
 * @param skipAlert
 * @param mode
 */
function toggleStateManagement(skipAlert, mode) {
    if (!$.layout.plugins.stateManagement) return;

    const options = verteilungLayout.options.stateManagement;
    let enabled = options.enabled // current setting
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
            alertify.success('This layout will reload as the options specify \nwhen the page is refreshed.');
    }
    else if (!skipAlert)
        alertify.success("This layout will save and restore its last state \nwhen the page is refreshed.");
}

/**
 * baut das Layout der Anwendung auf
 */
function loadLayout() {
    try {
        const tabs = $("#tabs");
        const clientPageLayoutSettings = {
            name: "clientLayoutSettings",
            center__paneSelector: "#clientPage",
            resizable: false
        };
        // Seitenlayout
        const pageLayoutSettings = {
            name: "pageLayout",
            spacing_open: 8,
            spacing_closed: 12,
            resizerClass: "ui-widget-content",
            togglerClass: "ui-widget-content",
            center: {
                paneSelector: "#tabs",
                size: "auto",
                resizeWithWindow: true
            },
            south: {
                paneSelector: "#contentSouth",
                contentSelector: ".ui-widget-content",
                size: 0.3,
                resizeWithWindow: true,
                onresize: function () {
                    if (Verteilung.outputEditor.editor)
                        Verteilung.outputEditor.editor.resize();
                }
            }
        };

        const contentLayoutSettings = {
            name: "contentLayout",
            spacing_open: 0,
            spacing_closed: 12,
            contentSelector: ".ui-widget-content",
            north: {
                paneSelector: "#tabNorth",
                size:33,
                children: {
                    name: "tabNorthInnerLayout",
                    center: {
                        name: "tabNorthInnerCenterLayout",
                        paneSelector: "#tabNorthInnerCenter"
                    }
                }
            },
            center: {
                size: "auto",
                paneSelector: "#tabPanels"
            },
            activate: $.layout.callbacks.resizeTabLayout
        };

         //AlfrescoTab
        const alfrescoLayoutSettings = {
            name: "alfrescoLayout",
            size: "auto",
            resizerTip: "Resize This Pane",
            livePaneResizing: true,
            spacing_open: 8,
            spacing_closed: 12,
            resizeWithWindow: false,
            contentSelector: ".ui-widget-content",
            resizerClass: "ui-widget-content",
            togglerClass: "ui-widget-content",
            north: {
                paneSelector: "#alfrescoNorth",
                name: "alfrescoNorthLayout",
                minSize: 30,
                maxSize: 30,
                resizable: false,
                closable: false,
                children: {
                    name: "alfrescoNorthInnerLayout",
                    center: {
                        name: "alfrescoNorthCenterLayout",
                        paneSelector: "#alfrescoNorthCenter"
                    }
                }
            },
            west: {
                paneSelector: "#alfrescoWest",
                name: "alfrescoWestLayout",
                size: .2,
                onresize: function(){
                    try {
                        $.fn.dataTable.tables({visible: true, api: true}).columns.adjust();
                    } catch (e) {
                        errorHandler(e);
                    }
                }
            },
            center: {
                paneSelector: "#alfrescoCenter",
                name: "alfrescoCenterLayout",
                minHeight: 80,
                size: .8,
                children: {
                    resizerTip: "Größe ändern",
                    resizerClass: "ui-widget-content",
                    togglerClass: "ui-widget-content",
                    spacing_open: 8,
                    spacing_closed: 12,
                    north: {
                        size: 225,
                        paneSelector: "#alfrescoCenterNorth",
                        name: "alfrescoCenterNorthLayout",
                        onresize: function () {
                            try {
                                $("#alfrescoFolderTabelle_wrapper>div.dataTables_scroll>div.dataTables_scrollBody").height($("#alfrescoCenterNorth").outerHeight() - ($("#alfrescoFolderTabelle_wrapper>div.dataTables_scroll>div.dataTables_scrollHead").outerHeight() + $("#alfrescoFolderTableFooter").outerHeight() + $("#alfrescoFolderTabelleHeader").outerHeight() +
                                    $("div ul li, .current").outerHeight() - 4));
                            } catch (e) {
                                errorHandler(e);
                            }
                        }
                    },
                    center: {
                        size: "auto",
                        paneSelector: "#alfrescoCenterCenter",
                        name: "alfrescoCenterCenterLayout",
                        onresize: function () {
                            try {
                                $("#alfrescoTabelle_wrapper>div.dataTables_scroll>div.dataTables_scrollBody").height($("#alfrescoCenterCenter").outerHeight() - ($("#alfrescoTabelle_wrapper>div.dataTables_scroll>div.dataTables_scrollHead").outerHeight() + $("#alfrescoTableFooter").outerHeight() + $("#alfrescoTabelleHeader").outerHeight() +
                                    $("div ul li, .current").outerHeight() - 4));
                            } catch (e) {
                                errorHandler(e);
                            }
                        }
                    }
                }
            }
        };
        // SearchTab
        const searchLayoutSettings = {
            name: "searchLayout",
            size: "auto",
            resizerTip: "Resize This Pane",
            livePaneResizing: true,
            spacing_open: 8,
            spacing_closed: 12,
            resizeWithWindow: false,
            contentSelector: ".ui-widget-content",
            resizerClass: "ui-widget-content",
            togglerClass: "ui-widget-content",
            north: {
                paneSelector: "#searchNorth",
                name: "searchNorthLayout",
                minSize: 30,
                maxSize: 30,
                resizable: false,
                closable: false,
                slidable: false
            },
            center: {
                paneSelector: "#searchCenter",
                name: "searchCenterLayout",
                size: "auto",
                onresize: function () {
                    try {
                        $("#alfrescoSearchTabelle_wrapper>div.dataTables_scroll>div.dataTables_scrollBody").height($("#searchCenter").outerHeight() - ($("#alfrescoSearchTabelle_wrapper>div.dataTables_scroll>div.dataTables_scrollHead").outerHeight() + $("#alfrescoSearchTableFooter").outerHeight() + $("#searchTabelleHeader").outerHeight() +
                            $("div ul li, .current").outerHeight() - 4));
                    } catch (e) {
                        errorHandler(e);
                    }
                }
            }
        };

        //VerteilungTab
        const verteilungLayoutSettings = {
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
                        if (Verteilung.textEditor.editor)
                            Verteilung.textEditor.editor.resize();
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
                    if (Verteilung.rulesEditor.editor)
                        Verteilung.rulesEditor.editor.resize();
                }
            },
            east: {
                size:.15,
                paneSelector: "#verteilungEast",
                onresize: function () {
                    if (Verteilung.propsEditor.editor)
                        Verteilung.propsEditor.editor.resize();
                }
            },
            //	enable state management
            stateManagement__enabled: false,
            showDebugMessages: true
        };


        // create the tabs before the page layout because tabs will change the height of the north-pane
        tabLayout = tabs.tabs({
            activate: function (event, ui) {
                $.layout.callbacks.resizeTabLayout(event, ui);
                // bei den nicht sichtbaren Panels ist die Height zunächst 0, so dass das Scrollen nicht funktioniert.
                // Deshalb wird die Height hier noch mal beim aktivieren gesetzt
                if (ui.newPanel.attr('id') === "tab1") {
                    if (!alfrescoFolderTabelle) {
                        buildAlfrescoTab();
                    }
                }
                if (ui.newPanel.attr('id') === "tab2") {
                    if (!alfrescoSearchTabelle)
                        buildSearchTab();
                    // $("#alfrescoSearchTabelle_wrapper>div.dataTables_scroll>div.dataTables_scrollBody").height($("#searchCenter").outerHeight() - ($("#alfrescoSearchTabelle_wrapper>div.dataTables_scroll>div.dataTables_scrollHead").outerHeight() + $("#alfrescoSearchTableFooter").outerHeight() + $("#searchTabelleHeader").outerHeight() +
                    //     $("div ul li, .current").outerHeight() - 1));
                    $('#alfrescoSearch').focus().select();
                }
                if (ui.newPanel.attr('id') === "tab3") {
                    if (!tabelle)
                        buildVerteilungTab();
                }
                //$.fn.dataTable.tables({visible: true, api: true}).columns.adjust().draw();
            },
            active: -1
        });



        const globalLayout = $('body').layout(clientPageLayoutSettings);
        $('#clientPage').layout(pageLayoutSettings);
        tabs.layout(contentLayoutSettings);


        alfrescoLayout = $('#tab1').layout(alfrescoLayoutSettings);
        searchLayout = $('#tab2').layout(searchLayoutSettings);
        verteilungLayout = $('#tab3').layout(verteilungLayoutSettings);


        globalLayout.deleteCookie();
        globalLayout.options.stateManagement.autoSave = false;
        // if there is no state-cookie, then DISABLE state management initially
        const cookieExists = !$.isEmptyObject(verteilungLayout.readCookie());
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
            const erg = executeService({"name": "openDocument", "errorMessage": "Dokument konnten nicht geöffnet werden!"}, [
                {"name": "documentId", "value": alfrescoTabelle.row($(obj).closest('tr')).data().objectID}
            ]);
            if (erg.success) {
                const file = new Blob([base64DecToArr(erg.data)], { type: erg.mimeType });
                const fileURL = URL.createObjectURL(file);
                window.open(fileURL);
            }

         } catch (e) {
            errorHandler(e);
        }
    }

    try {
        let duration;
        $.fn.dataTable.moment('DD.MM.YY');
        alfrescoTabelle = $('#alfrescoTabelle').DataTable({
            jQueryUI: false,
            dom: "rtiS",
            scrollY: 20,
            scroll: true,
            scroller: {loadingIndicator: true},
            scrollX: false,
            autoWidth: true,
            deferRender: true,
            lengthChange: false,
            searching: false,
            order: [[3, 'desc']],
            processing: true,
            serverSide: true,
            iconView: false,
            folderId : archivFolderId,
            withFolder: 1,
            ajax: {
                url: (window.location.pathname === "/context.html" ? "http://localhost:8080/Archiv" : window.location.pathname.substring(0, window.location.pathname.indexOf("/", 2))) + "/listFolderWithPagination",
                type: "POST",
                data: function (obj, meta) {
                     obj.folderId = meta.oInit.folderId;
                    obj.withFolder = meta.oInit.withFolder;
                    duration = new Date().getTime();
                    return JSON.stringify(obj);
                },
                 error:  function (xhr, status, error) {
                     try {
                         let txt;
                         const statusErrorMap = {
                             '400': "Server understood the request, but request content was invalid.",
                             '401': "Unauthorized access.",
                             '403': "Forbidden resource can't be accessed.",
                             '404': "Not found",
                             '500': "Internal server error.",
                             '503': "Service unavailable."
                         };
                         if (xhr.status) {
                             txt = statusErrorMap[xhr.status];
                             if (!txt) {
                                 txt = "Unknown Error \n.";
                             }
                         } else if (error === 'parsererror') {
                             txt = "Error.\nParsing JSON Request failed.";
                         } else if (error === 'timeout') {
                             txt = "Request Time out.";
                         } else if (error === 'abort') {
                             txt = "Request was aborted by the server";
                         } else {
                             txt = "Unknown Error \n.";
                         }
                         errorHandler(txt, "Server Error");

                     } catch (e) {
                         let str = "FEHLER:\n";
                         str = str + e.toString() + "\n";
                         for (let prop in e)
                             str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                         errorHandler(str, "Server Error");
                     }
                 },
                dataSrc: function (obj) {
                    Logger.log(Level.DEBUG, "Execution of Service: listFolderWithPagination duration " + (new Date().getTime() - duration) + " ms");
                    if (obj.data.length === 0) {
                        alfrescoViewModeMenu.superfish('disableItem','alfrescoAnsicht');
                        alfrescoDocumentSelectMenu.superfish('disableItem','alfrescoDocumentAuswahl');
                    }
                    else {
                        alfrescoViewModeMenu.superfish('enableItem','alfrescoAnsicht');
                        alfrescoDocumentSelectMenu.superfish('enableItem','alfrescoDocumentAuswahl');
                        alfrescoDocumentActionMenu.superfish('disableItem', 'alfrescoDocumentActionMove');
                        alfrescoDocumentActionMenu.superfish('disableItem', 'alfrescoDocumentActionDelete');
                        alfrescoDocumentSelectMenu.superfish('disableItem','alfrescoDocumentAuswahlRevert');
                        alfrescoDocumentSelectMenu.superfish('disableItem','alfrescoDocumentAuswahlNone');
                    }
                    if (obj.parent === archivFolderId || obj.parent === documentFolderId){
                        alfrescoDocumentActionMenu.superfish('disableItem', 'alfrescoDocumentActionUpload');
                    } else {
                        alfrescoDocumentActionMenu.superfish('enableItem', 'alfrescoDocumentActionUpload');
                    }
                    return obj.data;
                },
                dataType: "json",
                processData: false,
                contentType: 'application/json;charset=UTF-8'
            },
            select: {
                style: 'os'
            },
            columns: [
                {
                    class: 'alignCenter details-control awesomeEntity',
                    orderable: false,
                    data: null,
                    defaultContent: '',
                    width: "12px"
                },
                {
                    data: "contentStreamMimeType",
                    title: "Typ",
                    defaultContent: '',
                    type: "string",
                    class: "alignCenter alfrescoTableDragable",
                    width: "43px"
                },
                {
                    data: null,
                    title: "Vorschau",
                    orderable: false,
                    defaultContent: '',
                    type: "string",
                    class: "alignCenter",
                    width: "120px"
                },
                {
                    data: "title",
                    title: "Titel",
                    name: "cm:title, cmis:name",
                    defaultContent: '',
                    type: "string",
                    class: "alignLeft alfrescoTableDragable"
                },
                {
                    data: "documentDateDisplay",
                    title: "Datum",
                    name: "my:documentDate, cmis:creationDate",
                    defaultContent: '',
                    type: "string",
                    class: "alignLeft"
                },
                {
                    data: "person",
                    title: "Person",
                    name: "my:person",
                    defaultContent: '',
                    type: "string",
                    class: "alignLeft"
                },
                {
                    data: "amountDisplay",
                    title: "Betrag",
                    name: "my:amount",
                    defaultContent: '',
                    type: "string",
                    class: "alignLeft"
                },
                {
                    data: "idvalue",
                    title: "Schlüssel",
                    name: "my:idvalue",
                    defaultContent: '',
                    type: "string",
                    class: "alignLeft"
                },
                {
                    data: null,
                    title: "Aktion",
                    width: "120px",
                    class: "alignLeft"
                },
                {
                    data: "objectID"
                }
            ],
            columnDefs: [
                {
                    targets: [5, 6],
                    visible: true
                },

                {   targets: [9],
                    visible: false
                },

                {
                    targets: [1],
                    render: function (obj, type, row, meta) {
                        try {
                                if (obj && obj === "application/pdf" && !meta.settings.oInit.iconView) {
                                    let span = document.createElement("span");
                                    let image = document.createElement('div');
                                    image.id = "alfrescoTableIcon" + row.objectID;
                                    image.className = "alfrescoTableIconEvent alfrescoTableDragable treeDropable far fa-file-pdf fa-15x awesomeEntity";
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
                    visible: true
                },
                {
                    targets: [2],
                    render: function (obj, type, row, meta) {
                        try {
                            if (row && row.nodeRef && meta.settings.oInit.iconView) {
                                let span = document.createElement("span");

                                span.href = "#";
                                span.style.width = "100px";
                                span.style.height = "100px";
                                let image = document.createElement('img');
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
                    visible: false
                },
                {
                    targets: [3],
                    render: function (obj, type, row) {
                        if (obj)
                            return obj;
                        else if (row.name)
                            return row.name;
                        else
                            return "";
                    },
                    visible: true
                },
                {
                    targets: [4],
                    render: function (obj, type, row) {
                        if (row.documentDate) {
                            row.documentDateDisplay = $.datepicker.formatDate("dd.mm.yy", new Date(Number(row.documentDate)));
                        }  else if (row.creationDate)
                            row.documentDateDisplay = $.datepicker.formatDate("dd.mm.yy", new Date(Number(row.creationDate)));
                        else
                            row.documentDateDisplay = "";
                        return row.documentDateDisplay;
                    },
                    visible: true
                },
                {
                    targets: [6],
                    render: function (obj, type, row) {
                        if (row.amount) {
                            row.amountDisplay = $.format.number(row.amount, '#,##0.00');
                        } else {
                            row.amountDisplay = "";
                        }
                        return row.amountDisplay;
                    },
                    visible: true
                },
                {
                    targets: [8],
                    render: function(obj, types, row) {
                        return alfrescoAktionFieldFormatter(obj, types, row).outerHTML;
                    },
                    orderable: false
                }
            ],
            language: {
                info: "Zeigt Einträge _START_ bis _END_ von insgesamt _TOTAL_ \f",
                emptyTable: "",
                zeroRecords: "Keine Einträge!",
                infoEmpty:    "",
                 paginate: {
                     first: "Erste ",
                     last:  "Letzte ",
                     next:  "Nächste ",
                     previous: "Vorherige "
                 },
                select: {
                    rows: {
                        "_": " %d Zeilen markiert",
                        "1": " 1 Zeile markiert",
                        "0": ""
                    }
                }
            }
        }).on( 'select', function ( e, dt, type, indexes ) {
            if ( type === 'row' ) {
                alfrescoDocumentActionMenu.superfish('enableItem','alfrescoDocumentActionMove');
                alfrescoDocumentActionMenu.superfish('enableItem','alfrescoDocumentActionDelete');
                alfrescoDocumentSelectMenu.superfish('enableItem','alfrescoDocumentAuswahlRevert');
                alfrescoDocumentSelectMenu.superfish('enableItem','alfrescoDocumentAuswahlNone');
            }
        } ).on( 'deselect', function ( e, dt, type, indexes ) {
            if ( type === 'row' ) {
               if( alfrescoTabelle.rows( { selected: true }).length <= 1 ) {
                   alfrescoDocumentActionMenu.superfish('disableItem', 'alfrescoDocumentActionMove');
                   alfrescoDocumentActionMenu.superfish('disableItem', 'alfrescoDocumentActionDelete');
                   alfrescoDocumentSelectMenu.superfish('disableItem','alfrescoDocumentAuswahlRevert');
                   alfrescoDocumentSelectMenu.superfish('disableItem','alfrescoDocumentAuswahlNone');
               }
            }
        } ).on( 'draw', function () {
            $.fn.dataTable.makeEditable(alfrescoTabelle, updateInLineDocumentFieldDefinition());
        });

        $("#alfrescoTabelle_wrapper>div.dataTables_scroll>div.dataTables_scrollBody").height($("#alfrescoCenterCenter").outerHeight() - ($("#alfrescoTabelle_wrapper>div.dataTables_scroll>div.dataTables_scrollHead").outerHeight() + $("#alfrescoTableFooter").outerHeight() + $("#alfrescoTabelleHeader").outerHeight() +
            $("div ul li, .current").outerHeight() - 4));


        $("#alfrescoTabelle_info").detach().appendTo('#alfrescoTableFooter');
        $("#alfrescoTabelle_paginate").detach().appendTo('#alfrescoTableFooter');

        // Add event listener for opening and closing details
        $('#dtable2 tbody').on('click', 'td.details-control', function () {
            let tr = $(this).closest('tr');
            let row = alfrescoTabelle.row(tr);

            if (row.child.isShown()) {
                // This row is already open - close it
                row.child.hide();
                $(tr.get(0).childNodes[0]).removeClass('shown');
                //calculateTableHeight("alfrescoCenterCenterCenter", "dtable2", alfrescoTabelle, "alfrescoTabelleHeader", "alfrescoTableFooter");
            }
            else {
                // Open this row
                row.child(formatAlfrescoTabelleDetailRow(row.data())).show();
                $(tr.get(0).childNodes[0]).addClass('shown');
                //calculateTableHeight("alfrescoCenterCenterCenter", "dtable2", alfrescoTabelle, "alfrescoTabelleHeader", "alfrescoTableFooter");
            }
        });

        // Drag aus Tabelle
        $(document)
            .on('mousedown', '.alfrescoTableDragable', function (event) {
                try {
                    let nodes = [];
                    const selected = alfrescoTabelle.rows( {selected:true} ).data().toArray();
                    //prüfen, ob überhaupt etwas selektiert worden ist
                    if (!selected.length) {
                        const row = alfrescoTabelle.row($(this).closest(('tr')));
                        if (row && row.length)
                         selected.push(row.data());
                    }

                    for ( let index = 0; index < selected.length; ++index) {
                        if (selected[index])
                            nodes.push(selected[index].objectID)
                    }
                    if (nodes.length) {
                        const title = (selected.length > 1 ? (selected.length + " Dokumente") : selected[0].title ? selected[0].title : selected[0].name);
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
            jQueryUI: false,
            dom: "rtiS",
            scrollY: 20,
            scroll: true,
            scroller: {loadingIndicator: true},
            scrollX: false,
            autoWidth: true,
            deferRender: true,
            lengthChange: false,
            searching: false,
            processing: true,
            serverSide: true,
            folderId : archivFolderId,
            withFolder: -1,
            pageLength:20,
            ajax: {
                url: (window.location.pathname === "/context.html" ? "http://localhost:8080/Archiv" : window.location.pathname.substring(0, window.location.pathname.indexOf("/", 2))) + "/listFolderWithPagination",
                type: "POST",
                data: function (obj, meta) {
                    obj.folderId = meta.oInit.folderId;
                    obj.withFolder = meta.oInit.withFolder;
                    duration = new Date().getTime();
                    return JSON.stringify(obj);
                },
                error:  function (xhr, status, error) {
                    try {
                        let txt;
                        const statusErrorMap = {
                            '400': "Server understood the request, but request content was invalid.",
                            '401': "Unauthorized access.",
                            '403': "Forbidden resource can't be accessed.",
                            '404': "Not found",
                            '500': "Internal server error.",
                            '503': "Service unavailable."
                        };
                        if (xhr.status) {
                            txt = statusErrorMap[xhr.status];
                            if (!txt) {
                                txt = "Unknown Error \n.";
                            }
                        } else if (error === 'parsererror') {
                            txt = "Error.\nParsing JSON Request failed.";
                        } else if (error === 'timeout') {
                            txt = "Request Time out.";
                        } else if (error === 'abort') {
                            txt = "Request was aborted by the server";
                        } else {
                            txt = "Unknown Error \n.";
                        }
                        errorHandler(txt, "Server Error");

                    } catch (e) {
                        let str = "FEHLER:\n";
                        str = str + e.toString() + "\n";
                        for (let prop in e)
                            str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                        errorHandler(str, "Server Error");
                    }
                },
                dataSrc: function (obj) {
                    try {
                        Logger.log(Level.DEBUG, "Execution of Service: listFolderWithPagination duration " + (new Date().getTime() - duration) + " ms");
                        if (obj.data.length === 0) {
                            alfrescoFolderSelectMenu.superfish('disableItem','alfrescoFolderAuswahl');
                        }
                        else {
                            alfrescoFolderActionMenu.superfish('enableItem','alfrescoFolderAuswahl');
                            alfrescoFolderActionMenu.superfish('disableItem', 'alfrescoFolderActionCreate');
                            alfrescoFolderActionMenu.superfish('disableItem', 'alfrescoFolderActionMove');
                            alfrescoFolderActionMenu.superfish('disableItem', 'alfrescoFolderActionDelete');
                            alfrescoFolderSelectMenu.superfish('disableItem', 'alfrescoFolderAuswahlRevert');
                            alfrescoFolderSelectMenu.superfish('disableItem', 'alfrescoFolderAuswahlNone');
                        }
                        const tree = $.jstree.reference('#tree');
                        const parent = tree.get_node(obj.parent);
                        if (parent) {
                            fillBreadCrumb(parent.data);
                            //$("#tree").jstree(true).refresh_node(objectID);

                            // Knoten einfügen
                            for (let i = 0; i < obj.data.length; i++) {
                                if (!tree.get_node(obj.data[i].objectID))
                                    tree.create_node(parent, obj.data[i]);
                            }

                            tree.select_node(parent, true, false);
                            tree.open_node(parent);
                        }
                        return obj.data;
                    } catch (e) {
                        errorHandler(e);
                    }
                    
                },
                dataType: "json",
                processData: false,
                contentType: 'application/json;charset=UTF-8'
            },
            select: {
                style: 'os'
            },
            rowCallback: function( row, obj ) {
                try {
                    // Cell click
                    $('td', row).on('click', function () {
                        try {
                            if (this.cellIndex === 0) {
                                $("#tree").jstree('deselect_all', true);
                                switchAlfrescoDirectory(obj);
                            }
                        } catch (e) {
                            errorHandler(e);
                        }
                    });
                    // Cursor
                    $('td', row).on('mouseenter', function () {
                        if (this.cellIndex === 0)
                            $(this).css('cursor', 'pointer');
                    }).on('mouseleave', function () {
                        $(this).css('cursor', 'auto');

                    });
                } catch (e) {
                    errorHandler(e);
                }
            },
            order: [[2, 'desc']],
            columns: [
                {
                    class: 'alignCenter folder-control awesomeEntity treeDropable',
                    orderable: false,
                    data: null,
                    defaultContent: '',
                    width: "30px"
                },
                {
                    data: "name",
                    title: "Name",
                    name : "cm:title",
                    defaultContent: '',
                    type: "string",
                    createdCell: function(tableData, value, obj, row, column) {
                        if (obj.objectID === alfrescoRootFolderId ||
                            obj.objectID === archivFolderId ||
                            obj.objectID === fehlerFolderId ||
                            obj.objectID === unknownFolderId ||
                            obj.objectID === doubleFolderId ||
                            obj.objectID === documentFolderId ||
                            obj.objectID === inboxFolderId)
                            $(tableData).addClass("read_only");
                        else
                            $(tableData).removeClass("read_only");
                    },
                    class: "alignLeft alfrescoFolderTableDragable treeDropable"
                },
                {
                    data: "description",
                    title: "Beschreibung",
                    name : "cm:description",
                    defaultContent: '',
                    type: "string",
                    class: "alignLeft alfrescoFolderTableDragable treeDropable"
                },
                {
                    title: "Aktion",
                    data: null,
                    width: "120px",
                    class: "alignLeft",
                    orderable: false
                }
            ],
            columnDefs: [
                {
                    targets: [0],
                    orderable: false
                },
                {
                    targets: [1, 2],
                    visible: true
                },
                {
                    targets: [3],
                    render: function(data, type, row) {
                        return alfrescoFolderAktionFieldFormatter(data, type, row).outerHTML;
                    },
                    orderable: false
                }
            ],
            language: {
                info: "Zeigt Einträge _START_ bis _END_ von insgesamt _TOTAL_ \f",
                emptyTable: "",
                zeroRecords: "Keine Einträge!",
                infoEmpty:    "",
                paginate: {
                    first: "Erste ",
                    last:  "Letzte ",
                    next:  "Nächste ",
                    previous: "Vorherige "
                },
                select: {
                    rows: {
                        "_": " %d Zeilen markiert",
                        "1": " 1 Zeile markiert",
                        "0": ""
                    }
                }
            }
        }).on('select', function (e, dt, type, indexes) {
            try {
                if (type === 'row') {
                    if (
                    // der Ordner darf nicht der ArchivRootFolder sein
                    dt.data().objectID !== archivFolderId &&
                    // der Ordner darf nicht der Fehler Folder sein
                    dt.data().objectID !== fehlerFolderId &&
                    // der Ordner darf nicht der Inbox Folder sein
                    dt.data().objectID !== inboxFolderId &&
                    // der Ordner darf nicht der Unknown Folder sein
                    dt.data().objectID !== unknownFolderId &&
                    // der Ordner darf nicht der Doppelte Folder sein
                    dt.data().objectID !== doubleFolderId) {
                        alfrescoFolderActionMenu.superfish('enableItem', 'alfrescoFolderActionCreate');
                        if (
                        // der Ordner darf nicht der Dokumenten Folder sein
                        dt.data().objectID !== documentFolderId
                        ) {
                            alfrescoFolderActionMenu.superfish('enableItem', 'alfrescoFolderActionMove');
                            alfrescoFolderActionMenu.superfish('enableItem', 'alfrescoFolderActionDelete');
                        }
                    }
                    alfrescoFolderSelectMenu.superfish('enableItem', 'alfrescoFolderAuswahlRevert');
                    alfrescoFolderSelectMenu.superfish('enableItem', 'alfrescoFolderAuswahlNone');
                }
            } catch (e) {
                errorHandler(e);
            }
        }).on( 'deselect', function ( e, dt, type, indexes ) {
            try {
                if (type === 'row') {
                    if (alfrescoFolderTabelle.rows({selected: true}).length <= 1) {
                        alfrescoFolderActionMenu.superfish('disableItem', 'alfrescoFolderActionCreate');
                        alfrescoFolderActionMenu.superfish('disableItem', 'alfrescoFolderActionMove');
                        alfrescoFolderActionMenu.superfish('disableItem', 'alfrescoFolderActionDelete');
                        alfrescoFolderSelectMenu.superfish('disableItem', 'alfrescoFolderAuswahlRevert');
                        alfrescoFolderSelectMenu.superfish('disableItem', 'alfrescoFolderAuswahlNone');
                    }
                }
            } catch (e) {
                errorHandler(e);
            }
        } ).on( 'draw', function () {
            $.fn.dataTable.makeEditable(alfrescoTabelle, updateInLineDocumentFieldDefinition());
        });


        $("#alfrescoFolderTabelle_wrapper>div.dataTables_scroll>div.dataTables_scrollBody").height($("#alfrescoCenterNorth").outerHeight() - ($("#alfrescoFolderTabelle_wrapper>div.dataTables_scroll>div.dataTables_scrollHead").outerHeight() + $("#alfrescoFolderTableFooter").outerHeight() + $("#alfrescoFolderTabelleHeader").outerHeight() +
            $("div ul li, .current").outerHeight() - 4));


        // Drag aus Tabelle
        $(document)
            .on('mousedown', '.alfrescoFolderTableDragable', function (event) {
                try {
                    let nodes = [];
                    const selected = alfrescoFolderTabelle.rows( {selected:true} ).data().toArray();
                    //prüfen, ob überhaupt etwas selektiert worden ist
                    if (!selected.length) {
                        const row = alfrescoFolderTabelle.row($(this).closest(('tr')));
                        if (row && row.length)
                            selected.push(row.data());
                    }

                    for( let index = 0; index < selected.length; ++index) {
                        if (selected[index])
                            nodes.push(selected[index].objectID)
                    }
                    if (nodes.length) {
                        const title = (selected.length > 1 ? (selected.length + " Ordner") : selected[0].title ? selected[0].title : selected[0].name);
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
            const erg = executeService({"name": "openDocument", "errorMessage": "Dokument konnte nicht geöffnet werden!"}, [
                {"name": "documentId", "value": alfrescoSearchTabelle.row($(obj).closest('tr')).data().objectID}
            ]);
            if (erg.success) {
                const file = new Blob([base64DecToArr(erg.data)], { type: erg.mimeType });
                const fileURL = URL.createObjectURL(file);
                window.open(fileURL);
            }

        } catch (e) {
            errorHandler(e);
        }
    }

    try {
        let duration;
        $.fn.dataTable.moment('DD.MM.YY');
        alfrescoSearchTabelle = $('#alfrescoSearchTabelle').DataTable({
            jQueryUI: false,
            dom: "rtiS",
            scrollY: 20,
            scroll: true,
            scroller: {loadingIndicator: true},
            autoWidth: true,
            deferRender: true,
            lengthChange: false,
            searching: false,
            processing: true,
            serverSide: true,
            order: [[3, 'desc']],
            cmisQuery : "",
            iconView: false,
            ajax: {
                url: (window.location.pathname === "/context.html" ? "http://localhost:8080/Archiv" : window.location.pathname.substring(0, window.location.pathname.indexOf("/", 2))) + "/findDocumentWithPagination",
                type: "POST",
                data: function (obj, meta) {
                    obj.cmisQuery = meta.oInit.cmisQuery;
                    duration = new Date().getTime();
                    return JSON.stringify(obj);
                },
                error:  function (xhr, status, error) {
                    try {
                        let txt;
                        const statusErrorMap = {
                            '400': "Server understood the request, but request content was invalid.",
                            '401': "Unauthorized access.",
                            '403': "Forbidden resource can't be accessed.",
                            '404': "Not found",
                            '500': "Internal server error.",
                            '503': "Service unavailable."
                        };
                        if (xhr.status) {
                            txt = statusErrorMap[xhr.status];
                            if (!txt) {
                                txt = "Unknown Error \n.";
                            }
                        } else if (error === 'parsererror') {
                            txt = "Error.\nParsing JSON Request failed.";
                        } else if (error === 'timeout') {
                            txt = "Request Time out.";
                        } else if (error === 'abort') {
                            txt = "Request was aborted by the server";
                        } else {
                            txt = "Unknown Error \n.";
                        }
                        errorHandler(txt, "Server Error");

                    } catch (e) {
                        let str = "FEHLER:\n";
                        str = str + e.toString() + "\n";
                        for (let prop in e)
                            str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                        errorHandler(str, "Server Error");
                    }
                },
                dataSrc: function (obj) {
                    Logger.log(Level.DEBUG, "Execution of Service: findFolderWithPagination duration " + (new Date().getTime() - duration) + " ms");
                    if (obj.data.length === 0) {
                        searchViewModeMenu.superfish('disableItem','searchAnsicht');
                        searchDocumentSelectMenu.superfish('disableItem','alfrescoSearchDocumentAuswahl');
                      }
                    else {
                        searchViewModeMenu.superfish('enableItem','searchAnsicht');
                        searchDocumentSelectMenu.superfish('enableItem','alfrescoSearchDocumentAuswahl');
                        alfrescoSearchDocumentActionMenu.superfish('disableItem', 'alfrescoSearchDocumentActionMove');
                        alfrescoSearchDocumentActionMenu.superfish('disableItem', 'alfrescoSearchDocumentActionDelete');
                        searchDocumentSelectMenu.superfish('disableItem','alfrescoSearchDocumentAuswahlRevert');
                        searchDocumentSelectMenu.superfish('disableItem','alfrescoSearchDocumentAuswahlNone');
                    }
                    return obj.data;
                },
                dataType: "json",
                processData: false,
                contentType: 'application/json;charset=UTF-8'
            },
            select: {
                style: 'os'
            },
            columns: [

                {
                    class: 'alignCenter details-control awesomeEntity',
                    orderable: false,
                    data: null,
                    defaultContent: '',
                    width: "12px"
                },
                {
                    data: "contentStreamMimeType",
                    title: "Typ",
                    defaultContent: '',
                    type: "string",
                    class: "alignCenter",
                    width: "43px"
                },
                {
                    data: null,
                    title: "Vorschau",
                    orderable: false,
                    defaultContent: '',
                    type: "string",
                    class: "alignCenter",
                    width: "120px"
                },
                {
                    data: "title",
                    title: "Titel",
                    name: "cm:title, cmis:name",
                    defaultContent: '',
                    type: "string",
                    class: "alignLeft"
                },
                {
                    data: "documentDateDisplay",
                    title: "Datum",
                    name: "my:documentDate, cmis:creationDate",
                    defaultContent: '',
                    type: "string",
                    class: "alignLeft"
                },
                {
                    data: "person",
                    title: "Person",
                    name: "my:person",
                    defaultContent: '',
                    type: "string",
                    class: "alignLeft"
                },
                {
                    data: "amountDisplay",
                    title: "Betrag",
                    name: "my:amount",
                    defaultContent: '',
                    type: "string",
                    class: "alignLeft"
                },
                {
                    data: "idvalue",
                    title: "Schlüssel",
                    name: "my:idvalue",
                    defaultContent: '',
                    type: "string",
                    class: "alignLeft"
                },
                {
                    data: null,
                    title: "Aktion",
                    width: "130px",
                    class: "alignLeft"
                },
                {
                    data: "objectID"
                }
            ],
            columnDefs: [
                {
                    targets: [5, 6],
                    visible: true
                },

                {   targets: [9],
                    visible: false
                },

                {
                    targets: [1],
                    render: function (obj, type, row, meta) {
                        try {
                            if (obj && obj === "application/pdf" && !meta.settings.oInit.iconView) {
                                let span = document.createElement("span");
                                let image = document.createElement('div');
                                image.id = "alfrescoSearchTableIcon" + row.objectID;
                                image.className = "alfrescoSearchTableIconEvent far fa-file-pdf fa-15x awesomeEnity";
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
                    visible: true
                },
                {
                    targets: [2],
                    render: function (obj, type, row, meta) {
                        try {
                            if (obj && meta.settings.oInit.iconView) {
                                let span = document.createElement("span");

                                span.href = "#";
                                span.style.width = "100px";
                                span.style.height = "100px";
                                let image = document.createElement('img');
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
                    visible: false
                },
                {
                    targets: [3],
                    render: function (obj, type, row) {
                        if (obj)
                            return obj;
                        else if (row.name)
                            return row.name;
                        else
                            return "";
                    },
                    visible: true
                },
                {
                    targets: [4],
                    render: function (obj, type, row) {
                        if (row.documentDate) {
                            row.documentDateDisplay = $.datepicker.formatDate("dd.mm.yy", new Date(Number(row.documentDate)));
                        }  else if (row.creationDate)
                            row.documentDateDisplay = $.datepicker.formatDate("dd.mm.yy", new Date(Number(row.creationDate)));
                        else
                            row.documentDateDisplay = "";
                        return row.documentDateDisplay;
                    },
                    visible: true
                },
                {
                    targets: [6],
                    render: function (obj, type, row) {
                        if (row.amount) {
                            row.amountDisplay = $.format.number(row.amount, '#,##0.00');
                        } else {
                            row.amountDisplay = "";
                        }
                        return row.amountDisplay;
                    },
                    visible: true
                },
                {
                    targets: [8],
                    render: function(obj, type, row) {
                        
                        let container = alfrescoAktionFieldFormatter(obj, type, row);
                        let image = document.createElement("div");
                        image.href = "#";
                        image.className = "detailAim fas fa-bullseye fa-15x awesomeEntity";
                        image.title = "Dokument im Ordner anzeigen";
                        image.style.cursor = "pointer";
                        image.style.width = "16px";
                        image.style.height = "16px";
                        image.style.cssFloat = "left";
                        image.style.marginRight = "5px";
                        container.appendChild(image);
                        return container.outerHTML;
                    },
                    orderable: false
                }
            ],
            language: {
                info: "Zeigt Einträge _START_ bis _END_ von insgesamt _TOTAL_ \f",
                emptyTable: "Keine Ergebnisse gefunden",
                zeroRecords: "Keine Einträge!",
                infoEmpty:   "",
                paginate: {
                    first: "Erste ",
                    last:  "Letzte ",
                    next:  "Nächste ",
                    previous: "Vorherige "
                },
                select: {
                    rows: {
                        "_": " %d Zeilen markiert",
                        "1": " 1 Zeile markiert",
                        "0": ""
                    }
                }
            }
        }).on( 'select', function ( e, dt, type, indexes ) {
            if ( type === 'row' ) {
                alfrescoSearchDocumentActionMenu.superfish('enableItem','alfrescoSearchDocumentActionMove');
                alfrescoSearchDocumentActionMenu.superfish('enableItem','alfrescoSearchDocumentActionDelete');
                searchDocumentSelectMenu.superfish('enableItem','alfrescoSearchDocumentAuswahlRevert');
                searchDocumentSelectMenu.superfish('enableItem','alfrescoSearchDocumentAuswahlNone');
            }
        } ).on( 'deselect', function ( e, dt, type, indexes ) {
            if ( type === 'row' ) {
                if( alfrescoSearchTabelle.rows( { selected: true }).length <= 1 ) {
                    alfrescoSearchDocumentActionMenu.superfish('disableItem', 'alfrescoSearchDocumentActionMove');
                    alfrescoSearchDocumentActionMenu.superfish('disableItem', 'alfrescoSearchDocumentActionDelete');
                    searchDocumentSelectMenu.superfish('disableItem','alfrescoSearchDocumentAuswahlRevert');
                    searchDocumentSelectMenu.superfish('disableItem','alfrescoSearchDocumentAuswahlNone');
                }
            }
        } ).on( 'draw', function () {
            $.fn.dataTable.makeEditable(alfrescoSearchTabelle, updateInLineDocumentFieldDefinition());
        });

        $("#alfrescoSearchTabelle_wrapper>div.dataTables_scroll>div.dataTables_scrollBody").height($("#searchCenter").outerHeight() - ($("#alfrescoSearchTabelle_wrapper>div.dataTables_scroll>div.dataTables_scrollHead").outerHeight() + $("#alfrescoSearchTableFooter").outerHeight() + $("#searchTabelleHeader").outerHeight() +
            $("div ul li, .current").outerHeight() - 4));


        $("#alfrescoSearchTabelle_info").detach().appendTo('#alfrescoSearchTableFooter');
        $("#alfrescoSearchTabelle_paginate").detach().appendTo('#alfrescoSearchTableFooter');

        // Add event listener for opening and closing details
        $('#dtable4 tbody').on('click', 'td.details-control', function () {
            let tr = $(this).closest('tr');
            let row = alfrescoSearchTabelle.row(tr);

            if (row.child.isShown()) {
                // This row is already open - close it
                row.child.hide();
                tr.removeClass('shown');
                //calculateTableHeight("searchCenter", "dtable4", alfrescoSearchTabelle, "alfrescoSearchTabelleHeader", "alfrescoSearchTableFooter");
            }
            else {
                // Open this row
                row.child(formatAlfrescoTabelleDetailRow(row.data())).show();
                tr.addClass('shown');
                //calculateTableHeight("searchCenter", "dtable4", alfrescoSearchTabelle, "alfrescoSearchTabelleHeader", "alfrescoSearchTableFooter");
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
            //"pagingType": "paging_with_jqui_icons",
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
            const tr = $(this).closest('tr');
            const row = tabelle.row(tr);

            if (row.child.isShown()) {
                // This row is already open - close it
                row.child.hide();
                tr.removeClass('shown');
            }
            else {
                // Open this row
                row.child(formatVerteilungTabelleDetailRow(row.data())).show();
                tr.addClass('shown');
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

        let container = document.createElement("div");
        let image;
        let inner1;
        let inner2;

        // Ordner bearbeiten
        image = document.createElement("i");
        image.href = "#";
        image.className = "folderEdit fas fa-pencil-alt fa-15x awesomeEntity";
        image.title = "Ordner Details bearbeiten";
        image.style.cursor = "pointer";
        image.style.cssFloat = "left";
        image.style.marginRight = "5px";
        container.appendChild(image);
        
        // neuen Ordner im ausgewählten Ordner anlegen
        if (data.objectID !== alfrescoRootFolderId &&
            data.objectID !== archivFolderId &&
            data.objectID !== fehlerFolderId &&
            data.objectID !== unknownFolderId &&
            data.objectID !== doubleFolderId &&
            data.objectID !== inboxFolderId) {
            image = document.createElement("i");
            image.href = "#";
            //image.style.fontSize ="xx-small";
            image.className = "folderCreate fa-stack fa-09x";
            inner1 = document.createElement("i");
            inner1.className = "far fa-folder fa-stack-2x awesomeEntity" ;
            inner2 = document.createElement("i");
            inner2.className = " fas fa-plus fa-stack-1x awesomeAction";
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
        if (data.objectID !== alfrescoRootFolderId &&
            data.objectID !== archivFolderId &&
            data.objectID !== fehlerFolderId &&
            data.objectID !== unknownFolderId &&
            data.objectID !== doubleFolderId &&
            data.objectID !== inboxFolderId &&
            data.objectID !== documentFolderId) {
            image = document.createElement("i");
            image.href = "#";
            //image.style.fontSize ="xx-small";
            image.className = "folderRemove fa-stack fa-09x";
            inner1 = document.createElement("i");
            inner1.className = "far fa-folder fa-stack-2x awesomeEntity" ;
            inner2 = document.createElement("i");
            inner2.className = " fas fa-minus fa-stack-1x awesomeAction";
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
        let container = document.createElement("div");
        let image = document.createElement("i");
        image.href = "#";
        image.className = "detailEdit fas fa-pencil-alt fa-15x awesomeEntity";
        image.title = "Details bearbeiten";
        image.style.cursor = "pointer";
        image.style.cssFloat = "left";
        image.style.marginRight = "5px";
        container.appendChild(image);

        image = document.createElement("i");
        image.href = "#";

        if (data.commentCount > 0) {
            image.style.cursor = "pointer";
            if (data.commentCount === 1)
                image.className = "showComments fas fa-comment fa-15x awesomeEntity";
            else
                image.className = "showComments fas fa-comments fa-15x awesomeEntity";
        }
        else {
             image.className = "showComments far fa-comment fa-15x awesomeEntity";
            image.style.cursor = "none";
        }
        image.title = "Kommentare";
        image.style.cursor = "pointer";
        image.style.cssFloat = "left";
        image.style.marginRight = "5px";
        container.appendChild(image);

        image = document.createElement("i");
        image.href = "#";
        image.className = "moveDocument far fa-copy fa-15x awesomeEntity";
        image.title = "Dokument verschieben";
        image.style.cursor = "pointer";
        image.style.cssFloat = "left";
        image.style.marginRight = "5px";
        container.appendChild(image);

        image = document.createElement("i");
        image.href = "#";
        image.className = "deleteDocument far fa-trash-alt fa-15x awesomeEntity";
        image.title = "Dokument löschen";
        image.style.cursor = "pointer";
        image.style.cssFloat = "left";
        image.style.marginRight = "5px";
        container.appendChild(image);

        image = document.createElement("i");
        image.href = "#";
        image.className = "rulesDocument fab fa-wpforms fa-15x awesomeEntity";
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
        let container = document.createElement("div");
        let image = document.createElement("div");
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
        if (daten[full[1]]["notDeleteable"] !== "true") {
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
    let sOut = '<div class="innerDetails" style="overflow: auto; width: 100%; " ><table>' +
        '<tr><tr style="height: 0px;" > '+
        '<th style="width: 100px; padding-top: 0px; padding-bottom: 0px; border-top-width: 0px; border-bottom-width: 0px; height: 0px; font-size: 12px"' +
        'colspan="1" rowspan="1" tabindex="0" class="control center">Fehler</th>' +
        '<th style="width: auto; padding-left: 10px; padding-top: 0px; padding-bottom: 0px; border-top-width: 0px; border-bottom-width: 0px; height: 0px; font-size: 12px"' +
        'colspan="1" rowspan="1" tabindex="0" class="alignLeft">Beschreibung</th></tr><td>';
    let txt = "<tr>";
    for ( let i = 0; i < data[5].length; i++) {
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
    return 'Name: ' + data.name +
        ' erstellt am: ' + $.formatDateTime('dd.mm.yy hh:ii:ss', new Date(Number(data.creationDate))) +
        ' von: ' + data.createdBy +
        (data.lastModificationDate == data.creationDate ? '' : ' modifiziert am: ' + $.formatDateTime('dd.mm.yy hh:ii:ss', new Date(Number(data.lastModificationDate))) +
            ' von: ' + data.lastModifiedBy) +
        ' Version: ' + data.versionLabel + ' ' + (data.checkinComment ? data.checkinComment : '') +
        ' Position: ' + data.parents[0].path +
        ' Größe: ' + $.formatNumber(data.contentStreamLength, {format:"#,###", locale:"de"}) + ' Bytes';
}

/**
 * füllt die BreadCrumb Leiste
 * @param data          der aktuelle Folder
 */
function fillBreadCrumb(data) {
    try {
        let object;
        let id;
        let parentObj;
        let name;
        let fill = true;
        const tree = $("#tree").jstree(true);
        const oldLi = $('#breadcrumblist');
        if (oldLi)
            oldLi.remove();
        const container = $('#breadcrumb');
        let ul = document.createElement('ul');
        ul.id = 'breadcrumblist';
        do {
            if(data.path) {
                object = data.path.split('/');
                id = data.objectID;
                parentObj = data.parentId;
                name = data.name;

                let li = document.createElement('li');
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
                let a = document.createElement('a');
                a.href = '#';
                a.text = name;
                li.appendChild(a);
                // prüfen, ob ein Parent da ist
                if (!parentObj)
                    fill = false;
                else {
                    // Daten des Parents
                    data = tree.get_node(parentObj).data;
                    // wenn die nicht existieren sind wir im Root Knoten und können hier abbrechen
                    if (!data)
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
        fnShowError : function(text, aktion){
            alertify.alert("Fehler", text);
        },
        aoColumns: [ null,
            null,
            {
                placeholder: "",
                tooltip: 'Titel des Dokumentes'
            },
            {
                placeholder: "",
                tooltip: 'Datum des Dokumentes',
                type: 'datepicker',
                datepicker: {
                    dateFormat: "dd.mm.yy"
                }
            },
            {
                placeholder: "",
                tooltip: 'Zugeordnete Person',
                loadtext: 'lade...',
                type: 'select',
                onblur: 'submit',
                data: "{'':'Bitte auswählen...', 'Klaus':'Klaus','Katja':'Katja','Till':'Till', 'Kilian':'Kilian'}"
            },
            {
                placeholder: "",
                tooltip: 'Rechnungsbetrag des Dokumentes'
            },
            {
                placeholder: "",
                tooltip: 'Identifikationsschlüssel des Dokumentes'
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
        fnShowError: function (text, aktion) {
            alertify.alert("Fehler", text);
        },
        aoColumns: [null,
            {
                placeholder: ""
            },
            {
                placeholder: ""
            },
            null
        ],
        sUpdateURL: updateInlineFolder
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
        let changed = false;
        let oldValue = "";
        let data = alfrescoFolderTabelle.row($(this).closest('tr')).data();
        switch (this.cellIndex) {
            case 1: {                           // Name geändert
                if (value !== data.name) {
                    if (data.name)
                        oldValue = data.name;
                    data.name = value;
                    changed = true;
                }
                break;
            }
            case 2: {                           // Beschreibung geändert
                if (value !== data.description) {
                    if (data.description)
                        oldValue = data.description;
                    data.description = value;
                    changed = true;
                }
                break;
            }
        }
        if (changed) {
            const erg = editFolder(data, data.objectID);
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
        let changed = false;
        let convValue;
        let oldValue = "";
        const row = $(this.parentElement.parentElement.parentElement).DataTable().row(this);
        const data = row.data();
        switch (this.cellIndex) {
            case 2: {                                // Titel geändert
                if ( value !== data.title) {
                    if (data.titel)
                        oldValue = data.title;
                    data.title = value;
                    changed = true;
                }
                break;
            }
            case 3: {                               // Datum geändert
                convValue = $.datepicker.parseDate("dd.mm.yy", value).getTime();
                if (convValue !== data.documentDate){
                    if (data.documentDateDisplay)
                        oldValue = data.documentDateDisplay;
                    data.documentDate = convValue;
                    changed = true;
                }
                break;
            }
            case 4: {                              // Person geändert
                if (value !== data.person){
                    if (data.person)
                        oldValue = data.person;
                    data.person = value;
                    changed = true;
                }
                break;
            }
            case 5: {                             // Betrag geändert
                convValue = parseFloat(value.replace(/\./g, '').replace(/,/g, "."));
                if (convValue !== data.amount) {
                    if (data.amountDisplay)
                        oldValue = data.amountDisplay;
                    data.amount = convValue;
                    changed = true;
                }
                break;
            }
            case 6:{                             // Id geändert
                if (value !== data.idvalue){
                    if (data.idvalue)
                        oldValue = data.idvalue;
                    data.idvalue = value;
                    changed = true;
                }
                break;
            }
        }
        if (changed) {
            editDocument(data, data.objectID);

        }
        return value;
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
        let objectID;
        if (data)
            objectID = data.objectID;
        else
            objectID = "-1";
        alfrescoTabelle.settings().init().folderId = objectID;
        alfrescoTabelle.ajax.reload();
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
        alfrescoSearchTabelle.settings().init().cmisQuery = "select d.*, o.*, c.*, i.* from my:archivContent as d " +
            "join cm:titled as o on d.cmis:objectId = o.cmis:objectId " +
            "join my:amountable as c on d.cmis:objectId = c.cmis:objectId " +
            "join my:idable as i on d.cmis:objectId = i.cmis:objectId  WHERE IN_TREE(d, '" + archivFolderId + "') AND ( CONTAINS(d, 'cmis:name:*" + searchText + "* OR TEXT:" + searchText + "') OR CONTAINS(o, 'cm:title:*" + searchText + "*'))";
        alfrescoSearchTabelle.ajax.reload();
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
             const tr = $(this).closest('tr');
             const row = alfrescoFolderTabelle.row( tr).data();
             $("#tree").jstree('deselect_all', true);
             switchAlfrescoDirectory(row);
        } catch (e) {
            errorHandler(e);
        }
     });
    $(document).on("click", ".folderCreate", function () {
        try {
            const tr = $(this).closest('tr');
            const data = alfrescoFolderTabelle.row(tr).data();
            startFolderDialog(data, "bootstrap-create", true);
        } catch (e) {
            errorHandler(e);
        }
    });
    $(document).on("click", ".folderRemove", function () {
        try {
            const tr = $(this).closest('tr');
            const data = alfrescoFolderTabelle.row(tr).data();
            startFolderDialog(data, "web-display", true);
        } catch (e) {
            errorHandler(e);
        }
    });
    $(document).on("click", ".folderEdit", function () {
        try {
            const tr = $(this).closest('tr');
            const data = alfrescoFolderTabelle.row(tr).data();
            startFolderDialog(data, "web-edit", true);
        } catch (e) {
            errorHandler(e);
        }
    });
}


/**
 * sucht ein Dokument und ofnnet die entsprechenden  Knoten im Tree und Tabelle
 * @param data    das zu suchende Objekt
 * @return true falls erfolgreich
 */
function aimNode(data) {
    const results = [];
    const tree = $('#tree').jstree(true);
    const id = data.objectID;
    if (data && data.parents) {
        let node = tree.get_node(data.parents[0].objectId);
        // Alle Knoten bis hinauf zum ersten geöffneten Knoten suchen
        while (!node) {
            const json = executeService({
                "name": "getNodeById",
                "errorMessage": "Dokument konnten nicht gelesen werden!"
            }, [
                {"name": "documentId", "value": data.parentId}
            ]);
            if (json.success) {
                data = json.data;
                results.push(data);
                if (data && data.parentId)
                    node = tree.get_node(data.parentId);
            } else {
                break;
            }
        }
    }
    if (node) {
        // keine Chain gefunden, der Knoten war schon offen und kann direkt selektiert werden
        if (!results.length) {
            tree.deselect_all(true);
            tree.select_node(node, true);
            tree.open_node(node, function () {
                alfrescoTabelle.on('draw', function () {
                    alfrescoTabelle.off('draw');
                    const row = alfrescoTabelle.row('#' + id);
                    if (row && row.length) {
                        row.draw().show().draw(false);
                        row.select();
                    }
                });
                switchAlfrescoDirectory(node.data);
            });

        }
        // die Chain hoch hangeln
        else {
            results.push(node.data);
            results.reverse();
            // Hier muss mit einem Deferred Object gearbeitet werden, denn der open im Tree
            // bewirkt einen asynchronen Aufruf, so das die nachfolgenden Operationen sonst nicht
            // die notwendigen Daten haben.
            let deffereds = $.Deferred(function (def) {
                def.resolve();
            });

            for (let index = 0; index < results.length; index++) {
                deffereds = (function (name, last, id, deferreds) {
                    return deferreds.then(function () {
                        return $.Deferred(function (def) {
                            let node = tree.get_node(name.objectID);
                            tree.open_node(node, function (last) {

                                if (last) {

                                    node = tree.get_node(results[results.length - 1].objectID);
                                    tree.deselect_all(true);
                                    tree.select_node(node, true);
                                    tree.open_node(node, function () {
                                        alfrescoTabelle.on('draw', function () {
                                            alfrescoTabelle.off('draw');
                                            const row = alfrescoTabelle.row('#' + id);
                                            if (row && row.length) {
                                                row.draw().show().draw(false);
                                                 row.select();
                                            }

                                        });
                                        switchAlfrescoDirectory(node.data);

                                    });
                                }
                                def.resolve();
                            });
                        });
                    });
                })(results[index], index === results.length - 1, id, deffereds);
            }
        }
        return true;
    }
    return false;
}

/**
 * behandelt die Clicks auf die Icons in der Alfrescotabelle
 */
function handleAlfrescoImageClicks() {
    // Details bearbeiten
    $(document).on("click", ".detailEdit", function () {
        try {
            const tr = $(this).closest('tr');
            startDocumentDialog($('#' + tr[0].parentElement.parentElement.id).DataTable().row(tr).data(), "web-edit", true);
        } catch (e) {
            errorHandler(e);
        }
    });
    // Kommentare lesen
    $(document).on("click", ".showComments", function () {
        try {
            const tr = $(this).closest('tr');
            // Kommentare lesen
            const json = executeService({"name": "getComments", "errorMessage": "Kommentare konnten nicht gelesen werden!"}, [
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
            const tr = $(this).closest('tr');
            const table = $('#' + tr[0].parentElement.parentElement.id).DataTable();
            startMoveDialog([table.row(tr).data()], table);
        } catch (e) {
            errorHandler(e);
        }
    });
    // Dokument löschen
    $(document).on("click", ".deleteDocument", function () {
        try {
            const tr = $(this).closest('tr');
            startDocumentDialog($('#' + tr[0].parentElement.parentElement.id).DataTable().row(tr).data(), "web-display", true);
        } catch (e) {
            errorHandler(e);
        }
    });
    // Regeln
    $(document).on("click", ".rulesDocument", function () {
        try {
            const tr = $(this).closest('tr');
            const tabelle = $('#' + tr[0].parentElement.parentElement.id).DataTable();
            const id = tabelle.row(tr).data().objectID;
            const json1 = executeService({"name": "getDocumentContentExtracted", "errorMessage": "Dokument konnten nicht gelesen werden!"}, [
                {"name": "documentId", "value": id}
            ]);
            if (json1.success) {
                const json = executeService({
                    "name": "getDocumentContent",
                    "errorMessage": "Dokument konnten nicht gelesen werden!"
                }, [
                    {"name": "documentId", "value": id}
                ]);
                if (json.success) {
                    const binary_string =  window.atob(json.data);
                    const len = binary_string.length;
                    let bytes = new Uint8Array( len );
                    for (let i = 0; i < len; i++)        {
                        bytes[i] = binary_string.charCodeAt(i);
                    }
                    tabLayout.tabs("option", "active", 2);
                    loadText(bytes, json1.data, tabelle.row(tr).data().name, tabelle.row(tr).data().contentStreamMimeType, null);
                }
            }
        } catch (e) {
            errorHandler(e);
        }

    });
    // Ziel im Ordner suchen
    $(document).on("click", ".detailAim", function () {
        try {
           
            const tr = $(this).closest('tr');
            const tabelle = $('#' + tr[0].parentElement.parentElement.id).DataTable();
            const data = tabelle.row(tr).data();
            if (aimNode(data))
            // auf den Archiv Tab umschalten
               tabLayout.tabs("option", "active", 0);

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
            const tr = $(this).closest('tr');
            const row = tabelle.row(tr).data();
            const name = row[1];
            REC.currentDocument.setContent(daten[name]["text"]);
            REC.testRules(Verteilung.rulesEditor.editor.getSession().getValue());
            daten[name].log = REC.mess;
            daten[name].result = results;
            daten[name].position = REC.positions;
            daten[name].xml = REC.currXMLName;
            daten[name].error = REC.errors;
            const ergebnis = [];
            ergebnis["error"] = REC.errors.length > 0;
            row[2] = REC.currXMLName.join(" : ");
            row[3] = ergebnis;
            row[5] = REC.errors;
            if (tabelle.fnUpdate(row, aPos[0]) > 0)
                alertify.alert("Fehler", "Tabelle konnte nicht aktualisiert werden!");
        }
        catch (e) {
            errorHandler(e);
        }
    });
    $(document).on("click", ".glass", function () {
        try {
            const tr = $(this).closest('tr');
            const row = tabelle.row(tr).data();
            const name = row[1];
            multiMode = false;
            showMulti = true;
            Verteilung.textEditor.file = daten[name]["file"];
            document.getElementById('headerWest').textContent = Verteilung.textEditor.file;
            setXMLPosition(daten[name]["xml"]);
            Verteilung.textEditor.editor.getSession().setValue(daten[name]["text"]);
            Verteilung.propsEditor.editor.getSession().setValue(printResults(daten[name]["result"]));
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
            const answer = confirm("Eintrag löschen?");
            if (answer) {
                const tr = $(this).closest('tr');
                const row = tabelle.row(tr).data();
                const name = row[1];
                try {
                    netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
                } catch (e) {
                    alertify.alert("Fehler", "Permission to delete file was denied.");
                }
                Verteilung.textEditor.file = daten[name]["file"];
                Verteilung.textEditor.editor.getSession().setValue("");
                Verteilung.propsEditor.editor.getSession().setValue("");
                Verteilung.rulesEditor.editor.getSession().foldAll(1);
                if (Verteilung.textEditor.file) {
                    const file = Components.classes["@mozilla.org/file/local;1"].createInstance(Components.interfaces.nsILocalFile);
                    file.initWithPath(Verteilung.textEditor.file);
                    if (file.exists() === true)
                        file.remove(false);
                }
                tabelle.fnDeleteRow(aPos[0]);
            }
        } catch (e) {
            errorHandler(e);
        }
    });
    $(document).on("click", ".pdf", function () {
        try {
            const tr = $(this).closest('tr');
            const row = tabelle.row(tr).data();
            const name = row[1];
            const erg = executeService({"name": "openPDF", "errorMessage": "Dokument konnte nicht geöffnet werden!"}, [
                {"name": "fileName", "value": daten[name].file}
            ]);
            if (erg.success) {
                const file = new Blob([base64DecToArr(erg.data)], {type: erg.mimeType});
                const fileURL = URL.createObjectURL(file);
                window.open(fileURL);
            }
        } catch (e) {
            errorHandler(e);
        }
    });
    $(document).on("click", ".moveToInbox", function () {
        try {
            const tr = $(this).closest('tr');
            const row = tabelle.row(tr).data();
            const name = row[1];
            const docId = "workspace:/SpacesStore/" + daten[name]["container"];
            const json = executeService({"name": "createDocument", "errorMessage": "Dokument konnte nicht auf den Server geladen werden:", "successMessage": "Dokument " + name + " wurde erfolgreich in die Inbox verschoben!"}, [
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
    const item = {};
    if (data.baseTypeId === "cmis:folder") {
        // Eintrag ist vom Typ Folder
        item["icon"] = "";
        item["state"] = {"opened": false, "disabled": false, "selected": false};
        // Typen definieren
        item["type"] = "documentFolderStandard";
        if (data.objectID === alfrescoRootFolderId) {
            // Alfresco Root Folder
            item["type"] = "alfrescoRootFolderStandard";
        }
        if (data.objectID === archivFolderId) {
            // Archiv Folder
            item["type"] = "archivRootStandard";
        }
        if (data.objectID === inboxFolderId ||
            data.objectID === unknownFolderId) {
            // Die Standard Folder
            item["type"] = "archivFolderStandard";
        }
        if (data.objectID === fehlerFolderId) {
            // Fehler Folder
            item["type"] = "archivFehlerFolderStandard";
        }
        if (data.objectID === doubleFolderId) {
            // Fehler Folder
            item["type"] = "archivDoubleFolderStandard";
        }
        if (data.objectID === documentFolderId) {
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
    let obj = {};

    try {
        // keine Parameter mit gegeben, also den Rooteintrag erzeugen
        if (!aNode) {
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
                const done = function (json) {
                    if (json.success) {
                        obj = [];
                        for (let index = 0; index < json.data.length; index++) {
                            obj.push(buildObjectForTree(json.data[index]));
                        }
                        if (aNode.id === "#") {
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
                            if (json.data[0].parents[0])
                                obj[0].data = json.data[0].parents[0];
                        }
                        // CallBack ausführen
                        if (obj) {
                            callBack.call(this, obj);
                        }
                    }
                    else {
                        alertify.alert("Fehler", "Folder konnte nicht erfolgreich im Alfresco gelesen werden!");
                    }    
                };
                const json = executeService({"name": "listFolder", "callback": done, "errorMessage": "Verzeichnis konnte nicht aus dem Server gelesen werden:"}, [
                    {"name": "folderId", "value": aNode.id !== "#" ? aNode.id : archivFolderId},
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
        let sourceData, targetData;
        let erg = false;
        const t = $(data.event.target);
        // prüfen, ob das Element entweder auf eine passende Tabellenzeile (treeDropable)
        // oder auf einen Tree Knoten (jstree-anchor) gezogen werden soll.
        if (t.closest('.treeDropable').length || t.closest('.jstree-anchor').length) {
            for(let index = 0; index < data.data.nodes.length; ++index) {
                erg = false;
                if (data.data.jstree) {
                    // Quelle ist der Tree
                    sourceData = $.jstree.reference('#tree').get_node(data.data.nodes[index]).data;
                } else {
                    // Quelle ist die Tabelle
                    // die Daten aus der Tabellenzeile, also der Folder, in den verschoben werden soll
                    if (data.data.table === "alfrescoFolderTabelle") {
                        sourceData = alfrescoFolderTabelle.row('#' + data.data.nodes[index]).data();
                    } else if (data.data.table === "alfrescoTabelle") {
                        sourceData = alfrescoTabelle.row('#' + data.data.nodes[index]).data();
                    }
                }
                if (data.event.target.className.indexOf("jstree-anchor") !== -1) {
                    // Ziel ist der Tree
                    // der Zielknoten
                    targetData = $.jstree.reference('#tree').get_node(t).data;
                } else if (data.event.target.className.indexOf("treeDropable") !== -1 && data.event.target.className.indexOf("alfrescoTableDragable") === -1) {
                    // Ziel ist die Tabelle
                    // die Ziel Zeile
                    const row = alfrescoFolderTabelle.row(t.closest('.treeDropable')[0].parentElement);
                    if (row && row.length)
                        targetData = row.data();
                }
                // Object darf nicht in die Standard Ordner geschoben werden und die Standard Ordner dürfen generell nicht verschoben werden
                if (sourceData &&
                        // der zu verschiebene Ordner darf nicht der ArchivRootFolder sein
                    sourceData.objectID !== archivFolderId &&
                        // der zu verschiebene Ordner darf nicht der Dokumenten Folder sein
                    sourceData.objectID !== documentFolderId &&
                        // der zu verschiebene Ordner darf nicht derFehler Folder sein
                    sourceData.objectID !== fehlerFolderId &&
                        // der zu verschiebene Ordner darf nicht der Inbox Folder sein
                    sourceData.objectID !== inboxFolderId &&
                        // der zu verschiebene Ordner darf nicht der Unknown Folder sein
                    sourceData.objectID !== unknownFolderId &&
                        // der zu verschiebene Ordner darf nicht der Doppelte Folder sein
                    sourceData.objectID !== doubleFolderId &&
                    targetData &&
                        // Dokumente dürfen nicht direkt in den Dokumenten Ordner
                    (sourceData.baseTypeId === "cmis:folder" || (sourceData.baseTypeId === "cmis:document" && targetData.objectID !== documentFolderId)) &&
                        // Ziel für Objekte darf nicht AlfrescoRootFolder sein
                    targetData.objectID !== alfrescoRootFolderId &&
                        // Ziel für den Objekte darf nicht der ArchivRootFolder sein
                    targetData.objectID !== archivFolderId &&
                        // Ziel für Ordner darf nicht Fehler Folder sein
                    (sourceData.baseTypeId === "cmis:document" || (sourceData.baseTypeId === "cmis:folder" && targetData.objectID !== fehlerFolderId)) &&
                        // Ziel für den Ordner darf nicht der Folder für die Doppelten sein
                    (sourceData.baseTypeId === "cmis:document" || (sourceData.baseTypeId === "cmis:folder" && targetData.objectID !== doubleFolderId)) &&
                        // Ziel für den Ordner darf nicht  Unbekannten Folder sein
                    (sourceData.baseTypeId === "cmis:document" || (sourceData.baseTypeId === "cmis:folder" && targetData.objectID !== unknownFolderId)) &&
                        // Ziel für den Ordner darf nicht der Inbox Folder sein
                    (sourceData.baseTypeId === "cmis:document" || (sourceData.baseTypeId === "cmis:folder" && targetData.objectID !== inboxFolderId)) &&
                        // Ziel für den Ordner darf nicht Parent Folder sein, da ist er nämlich schon drin
                    targetData.objectID !== sourceData.parentId &&
                        // Ziel für den Ordner darf nicht derselbe oder ein eigener Child Folder sein, denn sonst würde der Knoten "entwurzelt"
                    getAllChildrenIds($.jstree.reference('#tree'), sourceData.objectID).indexOf(targetData.objectID) === -1) {
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
        let result = [];
        const node = treeObj.get_node(nodeId);
        result.push(node.id);
        if (node.children) {
            for(let i = 0; i < node.children.length; i++) {
                result = result.concat(getAllChildrenIds(treeObj, node.children[i]));
            }
        }
        return result;
    }

    function customMenu(node) {
        const tree = $("#tree").jstree(true);
        const items = {
            create: {
                separator_before: false,
                separator_after: false,
                label: "Erstellen",
                icon : "fas fa-plus-circle fa-1x",
                action: function (obj) {
                    try {
                        startFolderDialog($.jstree.reference('#tree').get_node(obj.reference[0]).data, "web-create", true);
                    } catch (e) {
                        errorHandler(e);
                    }
                }
            },
            rename: {
                separator_before: false,
                separator_after: false,
                label: "Ändern",
                icon : "fas fa-pencil-alt fa-1x",
                action: function (obj) {
                    try {
                        startFolderDialog($.jstree.reference('#tree').get_node(obj.reference[0]).data, "web-edit", true);
                    } catch (e) {
                        errorHandler(e);
                    }
                }
            },
            delete: {
                separator_before: false,
                separator_after: false,
                label: "Löschen",
                icon: "fas fa-trash-alt fa-1x",
                action: function (obj) {
                    try {
                        startFolderDialog($.jstree.reference('#tree').get_node(obj.reference[0]).data, "web-display", true);
                    } catch (e) {
                        errorHandler(e);
                    }
                }
            }
        };
        // Archivroot hat kein Kontextmenü
        if (tree.get_type(node) === "archivRootStandard")
            return false;
        // Im Fehlerordner kein Delete und Create
        if (tree.get_type(node) === "archivFehlerFolderStandard") {
            delete items.delete;
            delete items.create;
        }
        // In Standardordnern kein Delete und Create
        if (tree.get_type(node) === "archivFolderStandard") {
            delete items.delete;
            delete items.create;
        }
        // Im Ordner für die Dokumente kein Delete
        if (tree.get_type(node) === "archivDocumentFolderStandard") {
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
            core: {
                worker: window.location.pathname !== "/context.html", // für Test die Worker abschalten!
                data: function (node, aFunction) {
                    try {
                        // relevante Knoten im Alfresco suchen
                        loadAndConvertDataForTree(node, aFunction);
                    } catch (e) {
                        errorHandler(e);
                    }
                },
                error : function (err) {  
                    Logger.log(Level.DEBUG, err.reason);
                },
                check_callback: function (op, node, par, pos, more) {
                    try {
                        let erg = false;
                        // Umbenannt werden darf alles
                        if (op === "rename_node")
                            return true;
                        // Keine Verzeichnisse in die Archiv Standardordner verschieben (ausser Ordner Dokumente)
                        if ((op === "move_node" ||
                             op === "copy_node" ||
                             op === "create_node" ||
                             op === "delete_node") &&
                             node.data &&
                             node.data.objectID !== alfrescoRootFolderId &&
                             node.data.objectID !== archivFolderId &&
                             node.data.objectID !== inboxFolderId &&
                             node.data.objectID !== fehlerFolderId &&
                             node.data.objectID !== unknownFolderId &&
                             node.data.objectID !== doubleFolderId &&
                             node.data.objectID !== documentFolderId &&
                             node.data.baseTypeId === "cmis:folder" &&
                             par &&
                             par.id &&
                             par.id !== alfrescoRootFolderId &&
                             par.id !== archivFolderId &&
                             par.id !== inboxFolderId &&
                             par.id !== fehlerFolderId &&
                             par.id !== unknownFolderId &&
                             par.id !== doubleFolderId) {
                            erg = true;
                        }
                        // Dokumente nicht in die Root Verzeichenisse Archiv und Dokumente und nicht in das Verzeichnis wo es gerade ist.
                        if ((op === "move_node" ||
                             op === "copy_node") &&
                             node.data &&
                             node.data.baseTypeId === "cmis:document" &&
                             par &&
                             par.id &&
                             par.id !== alfrescoRootFolderId &&
                             par.id !== archivFolderId &&
                             par.id !== documentFolderId &&
                             par.id !== node.data.parentId ) {
                            erg = true;
                        }
                        return erg;
                    } catch (e) {
                        errorHandler(e);
                    }
                },
                themes: {
                    responsive: true,
                    variant: 'big',
                    stripes: false,
                    dots: true,
                    icons: true
                }
            },
            types : {
                '#' : {
                    max_children : 1
                },
                'archivRootStandard' : {
                    valid_children : ["archivFolderStandard", "archivDocumentFolderStandard", "archivFehlerFolderStandard"],
                    icon: "far fa-folder fa-15x awesomeEntity"
                },
                'archivFolderStandard' : {
                    valid_children : [],
                    icon: "far fa-folder fa-15x awesomeEntity"
                },
                'archivDoubleFolderStandard' : {
                    valid_children : [],
                    icon: "far fa-folder fa-15x awesomeEntity"
                },
                'archivFehlerFolderStandard' : {
                    valid_children : ["archivDoubleFolderStandard"],
                    icon: "far fa-folder fa-15x awesomeEntity"
                },
                'archivDocumentFolderStandard' : {
                    valid_children : ["documentFolderStandard"] ,
                    icon: "far fa-folder fa-15x awesomeEntity"
                },
                'documentFolderStandard' : {
                    valid_children : -1,
                    icon: "far fa-folder fa-15x awesomeEntity"
                }
            },
            contextmenu: {
                items: customMenu,
                select_node : false
            },
            plugins: ["dnd", "types", "contextmenu"]
        }).on("changed.jstree",  function (event, data){
            try {
                if (data.action === "select_node") {
                    const tree = $("#tree").jstree(true);
                    const evt = window.event || event;
                    const button = evt.which || evt.button;

                    // Select Node nicht bei rechter Maustaste
                    if (button !== 1 && ( typeof button !== "undefined")) {
                        return false;
                    }
                    if (!data.node || !data.node.data)
                    // für den Root Node
                        switchAlfrescoDirectory(null);
                    else {
                        if (data.node.data.baseTypeId === "cmis:folder") {
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
                if (data.node.id === inboxFolderId ) {
                    // Eventlistner für Drop in Inbox
                    const zone = document.getElementById(inboxFolderId);
                    zone.addEventListener('dragover', handleDragOver, false);
                    zone.addEventListener('drop', handleDropInbox, false);
                }
            } catch (e) {
                errorHandler(e);
            }
        }).on('move_node.jstree', function (event, data) {
            // Knoten innerhalb des Trees per Drag and Drop verschieben
            try {
                const nodeId = data.node.data.objectID;
                const parentId = data.node.data.parentId;
                const destinationId = data.parent;
                const done = function (json) {
                    if (json.success) {
                        const newData = json.data;
                        const source = json.source;
                        const target = json.target;
                        Logger.log(Level.INFO, "Ordner " + data.node.data.name + " von " + source.path + " nach " + target.path + " verschoben");
                        // Bei Bedarf den Ordner aus der Tabelle entfernen
                        const row = alfrescoFolderTabelle.row('#' + nodeId);
                        if (row && row.length) {
                            row.remove();
                            alfrescoFolderTabelle.draw();
                        }
                        // Bei Bedarf den neuen Ordner in die Tabelle einfügen
                        if (jQuery("#breadcrumblist").children().last().get(0).id === newData.parentId)
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
        }).on('open_node.jstree', function (event, data) {
            try {
                const icon = $('#' + data.node.id).find('i.jstree-icon.jstree-themeicon').first();
                icon.removeClass('fa-folder').addClass('fa-folder-open');
            } catch (e) {
                errorHandler(e);
            }
        }).on('close_node.jstree', function (event, data) {
            try {
                const icon = $('#' + data.node.id).find('i.jstree-icon.jstree-themeicon').first();
                icon.removeClass('fa-folder-open').addClass('fa-folder');
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
                    const erg = checkMove(data);
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
                    let sourceData, targetData;
                    const t = $(data.event.target);
                    if (t.closest('.treeDropable').length || t.closest('.jstree-anchor').length) {

                        for(let index = 0; index < data.data.nodes.length; ++index) {

                            if (data.data.jstree) {
                                // Quelle ist der Tree
                                sourceData = $.jstree.reference('#tree').get_node(data.data.nodes[index]).data;
                            } else {
                                // Quelle ist die Tabelle
                                // die Daten aus der Tabellenzeile, also der Folder, in den verschoben werden soll
                                if (data.data.table === "alfrescoFolderTabelle") {
                                    sourceData = alfrescoFolderTabelle.row('#' + data.data.nodes[index]).data();
                                } else if (data.data.table === "alfrescoTabelle") {
                                    const row = alfrescoTabelle.row('#' + data.data.nodes[index]);
                                    if (row && row.length)
                                        sourceData = row.data();
                                }
                            }
                            if (data.event.target.className.indexOf("jstree-anchor") !== -1) {
                                // Ziel ist der Tree
                                // der Zielknoten
                                targetData = $.jstree.reference('#tree').get_node(t).data;
                            } else if (data.event.target.className.indexOf("treeDropable") !== -1) {
                                // Ziel ist die Tabelle
                                // die Ziel Zeile
                                targetData = alfrescoFolderTabelle.row(t.closest('.treeDropable')[0].parentElement).data();
                            }
                            if (sourceData.baseTypeId === "cmis:folder") {
                                // Tree updaten
                                const sourceNode = $.jstree.reference('#tree').get_node(sourceData.objectID);
                                const targetNode = $.jstree.reference('#tree').get_node(targetData.objectID);
                                if (sourceNode && targetNode)
                                    $.jstree.reference('#tree').move_node(sourceNode, targetNode);
                                // Der Rest passiert im move_node Handler!!
                            } else {
                                const done = function(json) {
                                    if (json.success) {
                                        const source = json.source;
                                        const target = json.target;
                                        const text = "Dokument " + sourceData.name + " von " + source.path + " nach " + target.path + " verschoben";
                                        Logger.log(Level.INFO, text);
                                        const row = alfrescoTabelle.row('#' + json.data.DT_RowId);
                                        if (row) {
                                            row.remove();
                                            alfrescoTabelle.draw();
                                        }
                                        alertify.success(text);
                                    }
                                };
                                //Dokument wurde verschoben
                                const json = executeService({"name": "moveNode", "callback": done, "errorMessage": "Dokument konnte nicht verschoben werden:"}, [
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
     * @return {{erg, success}|    das Ergebnis}
     */
    function buildAlfrescoFolder(folder, id, txt) {
        let erg = executeService({"name": "getNodeId", "ignoreError": true}, [
            {"name": "filePath", "value": folder}
        ]);
        if (!erg.success) {
            const name = folder.split("/").pop();
            const extraProperties = {"my:archivFolder": {"cmis:name": "" + name +""}, "P:cm:titled":{"cm:title": "" + name +"", "cm:description":"" + txt + ""}};
            erg = executeService({"name": "createFolder"}, [
                {"name": "documentId", "value": id},
                {"name": "extraProperties", "value": extraProperties}
            ]);
            if (erg.success) {
                erg = executeService({"name": "getNodeId", "errorMessage": txt + " konnte nicht gefunden werden:"}, [
                    {"name": "filePath", "value": folder}
                ]);
                if (!erg.success)
                    Logger.log(Level.WARN, txt + " konnte auf dem Alfresco Server nicht gefunden werden!");
            }
        }
        return erg;
    }

    
    let ret, erg;
    // ist schon ein Alfresco Server verbunden?
    erg = executeService({"name": "isConnected"});
    if (erg.success && erg.data) {
        alfrescoServerAvailable = true;
    }
    else {
        // prüfen, ob Server ansprechbar ist
        if (getSettings("server"))
            alfrescoServerAvailable = checkServerStatus(getSettings("server"));
        // Ticket besorgen
        if (getSettings("user") && getSettings("password") && getSettings("server")) {
            //TODO
            erg = executeService({"name": "getTicketWithUserAndPassword"},
                [{"name": "user", "value": getSettings("user")},
                    {"name": "password", "value": getSettings("password")},
                    {"name": "server", "value": getSettings("server")}
                ]);
            if (erg.success) {
                // Binding prüfen
                if (alfrescoServerAvailable && getSettings("binding"))
                if (checkServerStatus(getSettings("binding") + "?alf_ticket=" + erg.data)) {
                    erg = executeService({"name": "setParameter", "errorMessage" : "Parameter für die Services konnten nicht gesetzt werden:"}, [
                        {"name": "server", "value": getSettings("server")},
                        {"name": "binding", "value": getSettings("binding")},
                        {"name": "user", "value": getSettings("user")},
                        {"name": "password", "value": getSettings("password")}
                    ]);
                    if (!erg.success) {
                        Logger.log(Level.WARN, "Binding Parameter konnten nicht gesetzt werden!");
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
         // Skript Verzeichnis prüfen
        const dataFolder = [];
        dataFolder['de'] = "/Datenverzeichnis/Skripte";
        dataFolder['en'] = "/Data Dictionary/Scripts";
        let langFound = "";
        for (let folder in dataFolder ) {
            erg = executeService({"name": "getNodeId", "ignoreError": true}, [
                {"name": "filePath", "value": dataFolder[folder]}
            ]);
            if (erg.success) {
                langFound = folder;
                break;
            }
        }
        if (erg.success)
            scriptFolderId = erg.data.objectID;
        else {
            Logger.log(Level.WARN, "Kein Verzeichnis für Skripte auf dem Alfresco Server  gefunden!");
        }
        // Verteilskript prüfen
        if (erg.success) {
            erg = executeService({"name" : "getNodeId", "ignoreError" : true}, [
                {"name": "filePath", "value": dataFolder[langFound] + "/recognition.js"}
            ]);
            if (!erg.success) {
                const script = $.ajax({
                    url: createPathToFile("./js/recognition.js"),
                    async: false
                }).responseText;

                if (script && script.length > 0) {
                    erg = executeService({
                        "name": "createDocument",
                        "errorMessage": "Verteilungsskript konnte nicht erstellt werden!"
                    }, [
                        {"name": "documentId", "value": scriptFolderId},
                        {"name": "fileName", "value": "recognition.js"},
                        {"name": "content", "value": script, "type": "byte"},
                        {"name": "mimeType", "value": "application/x-javascript"},
                        {
                            "name": "extraProperties",
                            "value":  {"cmis:document": {"cmis:name": "recognition.js"}, "P:cm:titled": {"cm:description": "Skript zum Verteilen der Dokumente" }}
                        },
                        {"name": "versionState", "value": "major"}
                    ]);
                     if (erg.success)
                        scriptID = erg.data.objectID;
                    else {
                        Logger.log(Level.WARN, "Verteilscript (recognition.js) konnte auf dem Alfresco Server nicht angelegt werden!");
                    }
                } else
                    Logger.log(Level.WARN, "Verteilscript (recognition.js) konnte nicht gelesen werden!");
            } else {
                scriptID = erg.data.objectID;
            }
        }
        if (erg.success) {
            // Regeln prüfen
            erg = executeService({"name" : "getNodeId", "ignoreError" : true}, [
                {"name": "filePath", "value": dataFolder[langFound] + "/doc.xml"}
            ]);
            if (!erg.success) {
                const doc = $.ajax({
                    url: createPathToFile("./rules/doc.xml"),
                    async: false
                }).responseText;
                if (doc && doc.length > 0) {
                    erg = executeService({
                        "name": "createDocument",
                        "errorMessage": "Verteilungsregeln konnten nicht erstellt werden!"
                    }, [
                        {"name": "documentId", "value": scriptFolderId},
                        {"name": "fileName", "value": "doc.xml"},
                        {"name": "content", "value": doc, "type": "byte"},
                        {"name": "mimeType", "value": "text/xml"},
                        {
                            "name": "extraProperties",
                            "value": {"cmis:document":{"cmis:name": "doc.xml"}, "P:cm:titled":{"cm:description":"Dokument mit den Verteil-Regeln"}}
                        },
                        {"name": "versionState", "value": "major"}

                    ]);
                    if (erg.success)
                        rulesID = erg.data.objectID;
                    else {
                        Logger.log(Level.WARN, "Verteilregeln (doc.xml) konnten auf dem Alfresco Server nicht angelegt werden!");
                    }
                } else
                    Logger.log(Level.WARN, "Verteilregeln (doc.xml) konnten nicht gelesen werden!");
            } else {
                rulesID = erg.data.objectID;
            }
        }
        if (erg.success) {
            // Schema prüfen
            erg = executeService({"name" : "getNodeId", "ignoreError" : true}, [
                {"name": "filePath", "value": dataFolder[langFound] + "/doc.xsd"}
            ]);
            if (!erg.success) {
                const doc = $.ajax({
                    url: createPathToFile("./rules/doc.xsd"),
                    async: false
                }).responseText;
                if (doc && doc.length > 0) {
                    erg = executeService({
                        "name": "createDocument",
                        "errorMessage": "Verteilungsschema konnten nicht erstellt werden!"
                    }, [
                        {"name": "documentId", "value": scriptFolderId},
                        {"name": "fileName", "value": "doc.xsd"},
                        {"name": "content", "value": doc, "type": "byte"},
                        {"name": "mimeType", "value": "text/xml"},
                        {
                            "name": "extraProperties",
                            "value": {"cmis:document":{"cmis:name": "doc.xsd"}, "P:cm:titled":{"cm:description":"Dokument mit den Verteilschema"}}
                        },
                        {"name": "versionState", "value": "major"}

                    ]);
                    if (erg.success)
                        rulesSchemaId = erg.data.objectID;
                    else {
                        Logger.log(Level.WARN, "Verteilschema (doc.xsd) konnten auf dem Alfresco Server nicht angelegt werden!");
                    }
                } else
                    Logger.log(Level.WARN, "Verteilschema (doc.xsd) konnten nicht gelesen werden!");
            } else {
                rulesSchemaId = erg.data.objectID;
            }
        }
        if (erg.success) {
            // Archiv prüfen
            erg = executeService({"name" : "getNodeId", "errorMessage" : "Archiv konnte nicht gefunden werden:"}, [
                {"name": "filePath", "value": "/"}
            ]);
            if (erg.success) {
                alfrescoRootFolderId = erg.data.objectID;
            } else {
                Logger.log(Level.WARN, "Root konnte auf dem Server nicht gefunden werden!");
            }
        }
        if (erg.success) {
            erg = buildAlfrescoFolder("/Archiv", alfrescoRootFolderId, "Der Archiv Root Ordner");
            if (erg.success)
                archivFolderId = erg.data.objectID;
            else
                Logger.log(Level.WARN, "Archiv konnte auf dem Alfresco Server nicht gefunden werden!");

        }
        if (erg.success) {
            // Archiv Root prüfen
            erg = buildAlfrescoFolder("/Archiv/Dokumente", archivFolderId, "Der Ordner für die abgelegten Dokumente");
            if (erg.success) {
                documentFolderId = erg.data.objectID;
            }
        }
        if (erg.success) {
            // Inbox prüfen
            erg = buildAlfrescoFolder("/Archiv/Inbox", archivFolderId, "Der Posteingangsordner");
            if (erg.success) {
                inboxFolderId = erg.data.objectID;
            }
        }
        if (erg.success) {
            // Fehlerbox prüfen
            erg = buildAlfrescoFolder("/Archiv/Fehler", archivFolderId, "Der Ordner für nicht verteilbare Dokumente");
            if (erg.success) {
                fehlerFolderId = erg.data.objectID;
            }
        }
        if (erg.success) {
            // Unbekanntbox prüfen
            erg = buildAlfrescoFolder("/Archiv/Unbekannt", archivFolderId, "Der Ordner für unbekannte Dokumente");
            if (erg.success) {
                unknownFolderId = erg.data.objectID;
            }
        }
        if (erg.success) {
            // Doppelte Box prüfen
            erg = buildAlfrescoFolder("/Archiv/Fehler/Doppelte", fehlerFolderId, "Verzeichnis für doppelte Dokumente");
            if (erg.success) {
                doubleFolderId = erg.data.objectID;
            }
        }

        ret = erg.success;

    } else {

        ret = false;
    }
    return ret;
}



/**
 * initialisiert die Anwendung
 */
function initApplication() {
    let erg;
    Logger.setLevel(Level.WARN);
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
        if (!getSettings("server") || !getSettings("binding") || !getSettings("user") || !getSettings("password")) {
            const cookie = $.cookie("settings");
            // prüfen, ob ein Cookie vorhanden ist
            if (cookie) {
                // Cookie ist vorhanden, also die Daten aus diesem verwenden
                settings = $.parseJSON(cookie);
            } else {
                settings = {settings:[]};
            }
        }
        if (!checkAndBuidAlfrescoEnvironment())
            tabLayout.tabs({
                disabled: [0, 1],
                active: 2
            });
        else
            tabLayout.tabs({
                disabled: [],
                active: 0
            });


        $.fn.dataTable.ext.errMode = function ( settings, helpPage, error ) {
            alertify.alert(error);
        };
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * baut die Memüs für den Alfresco Tab auf
 */
function createAlfrescoMenus() {
    alfrescoDocumentSelectMenu = $('#selectionMenuAlfrescoDocuments').superfish({
        menuData: {
            alfrescoDocumentAuswahl: {
                title: "Auswählen",
                className: "fas fa-check fa-1x",
                alfrescoDocumentAuswahlAll: {
                    title: "Alle",
                    className: "fas fa-check-circle fa-1x",
                    action: function () {
                        alfrescoTabelle.rows().select();
                    }
                },
                alfrescoDocumentAuswahlRevert: {
                    title: "Umkehren",
                    className: "fas fa-sync-alt fa-1x",
                    disabled: true,
                    action: function () {
                        const rows = alfrescoTabelle.rows({selected: true})[0];
                        alfrescoTabelle.rows().select();
                        alfrescoTabelle.rows(rows).deselect();
                    }
                },
                alfrescoDocumentAuswahlNone: {
                    title: "Keine",
                    className: "fas fa-times-circle fa-1x",
                    disabled: true,
                    action: function () {
                        alfrescoTabelle.rows().deselect();
                    }
                }
            }
        }
    });

    alfrescoDocumentActionMenu = $('#actionMenuAlfrescoDocuments').superfish({
        menuData: {
            "alfrescoDocumentAction": {
                title: "Aktion",
                className: "fas fa-sign-in-alt fa-1x",
                alfrescoDocumentActionUpload: {
                    title: "Upload",
                    className: "fas fa-upload fa-1x",
                    file: true,
                    action: function (evt) {
                        try {
                            evt.stopPropagation();
                            evt.preventDefault();
                            const files = evt.target.files;
                            const data = {
                                "name": files[0].name,
                                "title": files[0].name,
                                "file": files[0],
                                "documentDate": files[0].lastModifiedDate
                            };
                            startDocumentDialog(data, "web-create", true);
                        } catch (e) {
                            errorHandler(e);
                        }
                    }
                },
                alfrescoDocumentActionMove: {
                    title: "Verschieben",
                    className: "fas fa-copy fa-1x",
                    disabled: true,
                    action: function () {
                        try {
                            const data = alfrescoTabelle.rows({selected: true}).data();
                            startMoveDialog(data, alfrescoTabelle, "Dokumente");
                        } catch (e) {
                            errorHandler(e);
                        }
                    }
                },
                alfrescoDocumentActionDelete: {
                    title: "Löschen",
                    className: "fas fa-trash-alt fa-1x",
                    disabled: true,
                    action: function () {
                        let txt = "";
                        const selected = alfrescoTabelle.rows({selected: true}).data();
                        for (let i = 0; i < selected.length; i++) {
                            txt = txt + "<br>" + selected[i].name + (selected[i].title ? " [" + selected[i].title + "]" : "");
                        }
                        const titel = (selected.length > 1 ? "Dokumente löschen" : "Dokument löschen");
                        const text = (selected.length > 1 ? "Die folgenden Dokumente werden gelöscht:"  : "Das folgende Dokument wird gelöscht:") + txt;
                        alertify.confirm(titel, text,
                            function () {
                                let data = [];
                                for (let i = 0; i < selected.length; i++) {
                                    data.push(selected[i]);
                                }
                                deleteDocument(data);
                            },
                            function () {

                            });
                    }
                }
            }
        }
    });

    alfrescoViewModeMenu = $('#viewMenuAlfresco').superfish({
        menuData: {
            alfrescoAnsicht: {
                title: "Ansicht",
                className: "fas fa-file-alt fa-1x",
                alfrescoViewModeMenuNormal: {
                    title: "Normal",
                    className: "fas fa-file-alt fa-1x",
                    action: function () {
                        try {
                            alfrescoTabelle.settings().init().iconView = false;
                            alfrescoViewModeMenu.get(0).children[0].children[0].setAttribute('class', 'far fa-file fa-1x');
                            alfrescoViewModeMenu.children('li:first').superfish('hide');
                            alfrescoTabelle.column(0).visible(true);
                            alfrescoTabelle.column(1).visible(true);
                            alfrescoTabelle.column(2).visible(false);
                        } catch (e) {
                            errorHandler(e);
                        }
                    }
                },
                alfrescoViewModeMenuIcon: {
                    title: "Icons",
                    className: "fas fa-image fa-1x",
                    action: function () {
                        try {
                            alfrescoTabelle.settings().init().iconView = true;
                            alfrescoViewModeMenu.get(0).children[0].children[0].setAttribute('class', 'far fa-image fa-1x');
                            alfrescoViewModeMenu.children('li:first').superfish('hide');
                            alfrescoTabelle.column(0).visible(false);
                            alfrescoTabelle.column(1).visible(false);
                            alfrescoTabelle.column(2).visible(true);
                            alfrescoTabelle.columns(2).draw("page");
                        } catch (e) {
                            errorHandler(e);
                        }
                    }
                }
            }
        }
    });
}


/**
 * baut die Menüs für die Folder auf
 */
function createAlfrescoFolderMenus() {

     alfrescoFolderSelectMenu = $('#selectionMenuAlfrescoFolder').superfish({
        menuData: {
            alfrescoFolderAuswahl: {
                title: "Auswählen",
                className: "fas fa-check fa-1x",
                alfrescoFolderAuswahlAll: {
                    title: "Alle",
                    className: "far fa-check-circle fa-1x",
                    action: function () {
                        alfrescoFolderTabelle.rows().select();
                    }
                },
                alfrescoFolderAuswahlRevert: {
                    title: "Umkehren",
                    className: "fas fa-sync-alt fa-1x",
                    disabled: true,
                    action: function () {
                        const rows = alfrescoFolderTabelle.rows({selected: true})[0];
                        alfrescoFolderTabelle.rows().select();
                        alfrescoFolderTabelle.rows(rows).deselect();
                    }
                },
                alfrescoFolderAuswahlNone: {
                    title: "Keine",
                    className: "far fa-times-circle fa-1x",
                    disabled: true,
                    action: function () {
                        alfrescoFolderTabelle.rows().deselect();
                    }
                }
            }
        }
    });

    alfrescoFolderActionMenu = $('#actionMenuAlfrescoFolder').superfish({
        menuData: {
            "alfrescoFolderAction": {
                title: "Aktion",
                className: "fas fa-sign-in-alt fa-1x",
                alfrescoFolderActionCreate: {
                    title: "Erstellen",
                    className: "fas fa-plus-circle fa-1x",
                    disabled: true,
                    action: function () {
                        try {
                            const data = alfrescoFolderTabelle.rows({selected: true}).data();
                            startFolderDialog(data[0], "web-create", true);
                        } catch (e) {
                            errorHandler(e);
                        }
                    }
                },
                alfrescoFolderActionMove: {
                    title: "Verschieben",
                    className: "far fa-copy fa-1x",
                    disabled: true,
                    action: function () {
                        try {
                            const data = alfrescoFolderTabelle.rows({selected: true}).data();
                            startMoveDialog(data, alfrescoFolderTabelle, "Ordner");
                        } catch (e) {
                            errorHandler(e);
                        }
                    }
                },
                alfrescoFolderActionDelete: {
                    title: "Löschen",
                    className: "far fa-trash-alt fa-1x",
                    disabled: true,
                    action: function () {
                        let txt = "";
                        const selected = alfrescoFolderTabelle.rows({selected: true}).data();
                        for ( let i = 0; i < selected.length; i++) {
                            txt =  txt + "<br>" + selected[i].name;
                        }
                        const titel = "Ordner löschen";
                        const text = (selected.length > 1 ? "Die folgenden Ordner werden gelöscht:"  : "Der folgende Ordner wird gelöscht:") + txt;
                        alertify.confirm(titel, text,
                            function() {
                                let data = [];
                                for ( let i = 0; i < selected.length; i++) {
                                    data.push(selected[i]);
                                }
                                deleteFolder(data);
                            },
                            function(){

                            });
                    }
                }
            }
        }
    });

}

/**
 * baut die Menüs für die Suchseite auf
 */
function createSearchMenus(){

    searchViewModeMenu = $('#viewMenuSearch').superfish({
        menuData: {
            searchAnsicht: {
                title: "Ansicht",
                className: "fas fa-file-alt fa-1x",
                searchViewModeMenuNormal: {
                    title: "Normal",
                    className: "fas fa-file-alt fa-1x",
                    action: function () {
                        try {
                            alfrescoSearchTabelle.settings().init().iconView = false;
                            searchViewModeMenu.get(0).children[0].children[0].setAttribute('class', 'far fa-file-alt fa-1x');
                            searchViewModeMenu.children('li:first').superfish('hide');
                            alfrescoSearchTabelle.column(0).visible(true);
                            alfrescoSearchTabelle.column(1).visible(true);
                            alfrescoSearchTabelle.column(2).visible(false);
                        } catch (e) {
                            errorHandler(e);
                        }
                    }
                },
                searchViewModeMenuIcon: {
                    title: "Icons",
                    className: "fas fa-image fa-1x",
                    action: function () {
                        try {
                            alfrescoSearchTabelle.settings().init().iconView = true;
                            searchViewModeMenu.get(0).children[0].children[0].setAttribute('class', 'far fa-image fa-1x');
                            searchViewModeMenu.children('li:first').superfish('hide');
                            alfrescoSearchTabelle.column(0).visible(false);
                            alfrescoSearchTabelle.column(1).visible(false);
                            alfrescoSearchTabelle.column(2).visible(true);
                            alfrescoSearchTabelle.columns(2).draw("page");
                        } catch (e) {
                            errorHandler(e);
                        }
                    }
                }
            }
        }
    });
    alfrescoSearchDocumentActionMenu = $('#actionMenuAlfrescoSearchDocuments').superfish({
        menuData: {
            alfrescoSearchDocumentAction: {
                title: "Aktion",
                className: "fas fa-sign-in-alt fa-1x",
                alfrescoSearchDocumentActionMove: {
                    title: "Verschieben",
                    className: "fas fa-copy fa-1x",
                    disabled: true,
                    action: function () {
                        try {
                            const data = alfrescoSearchTabelle.rows({selected: true}).data();
                            startMoveDialog(data);
                        } catch (e) {
                            errorHandler(e);
                        }
                    }
                },
                alfrescoSearchDocumentActionDelete: {
                    title: "Löschen",
                    className: "fas fa-trash-alt fa-1x",
                    disabled: true,
                    action: function () {
                        let txt = "";
                        const selected = alfrescoSearchTabelle.rows({selected: true}).data();
                        for (let i = 0; i < selected.length; i++) {
                            txt = txt + "<br>" + selected[i].name + (selected[i].title ? " [" + selected[i].title + "]" : "");
                        }
                        const titel = (selected.length > 1 ? "Dokumente löschen" : "Dokument löschen");
                        const text = (selected.length > 1 ? "Die folgenden Dokumente werden gelöscht:"  : "Das folgende Dokument wird gelöscht:") + txt;
                        alertify.confirm(titel, text,
                            function () {
                                let data = [];
                                for (let i = 0; i < selected.length; i++) {
                                    data.push(selected[i]);
                                }
                                deleteDocument(data);
                            },
                            function () {

                            });
                    }
                }
            }
        }
    });
    searchDocumentSelectMenu = $('#selectionMenuAlfrescoSearchDocuments').superfish({
        menuData: {
            alfrescoSearchDocumentAuswahl: {
                title: "Auswählen",
                className: "fas fa-check fa-1x",
                alfrescoSearchDocumentAuswahlAll: {
                    title: "Alle",
                    className: "fas fa-check-circle fa-1x",
                    action: function () {
                        alfrescoSearchTabelle.rows().select();
                    }
                },
                alfrescoSearchDocumentAuswahlRevert: {
                    title: "Umkehren",
                    className: "fas fa-sync-alt fa-1x",
                    disabled: true,
                    action: function () {
                        const rows = alfrescoSearchTabelle.rows({selected: true})[0];
                        alfrescoSearchTabelle.rows().select();
                        alfrescoSearchTabelle.rows(rows).deselect();
                    }
                },
                alfrescoSearchDocumentAuswahlNone: {
                    title: "Keine",
                    className: "fas fa-times-circle fa-1x",
                    disabled: true,
                    action: function () {
                        alfrescoSearchTabelle.rows().deselect();
                    }
                }
            }
        }
    });
}

/**
 * baut die Alfresco Seite auf
 */
function buildAlfrescoTab(){

    createAlfrescoFolderMenus();
    createAlfrescoMenus();
    loadAlfrescoTree();
    // Eventhandler für die Image Clicks
    handleAlfrescoFolderImageClicks();
    handleAlfrescoImageClicks();

}

/**
 * baut die Suchseite auf
 */
function buildSearchTab() {
    
    createSearchMenus();
    loadAlfrescoSearchTable();

    $('#alfrescoSearch').on('keypress', function (event) {
        if(event.which === 13){
            startSearch($(this).val());
        }
    });
    // $("#alfrescoSearchButton").button({
    //     icons: {
    //         primary: 'ui-icon-search'
    //     }
    // });

}

/**
 * baut die Vereilungsseite auf
 */
function buildVerteilungTab(){

    Verteilung.propsEditor.editor = ace.edit("inProps");
    Verteilung.propsEditor.editor.setReadOnly(true);
    Verteilung.propsEditor.editor.renderer.setShowGutter(false);
    Verteilung.propsEditor.editor.setShowPrintMargin(false);
    Verteilung.propsEditor.editor.$blockScrolling = Infinity;
    Verteilung.propsEditor.fontsize = Verteilung.propsEditor.editor.getFontSize();
    let zoneRules = document.getElementById('inRules');
    zoneRules.addEventListener('dragover', handleDragOver, false);
    zoneRules.addEventListener('drop', handleRulesSelect, false);

    Verteilung.rulesEditor.editor = ace.edit("inRules");
    Verteilung.rulesEditor.editor.getSession().setMode("ace/mode/xml");
    Verteilung.rulesEditor.editor.setShowPrintMargin(false);
    Verteilung.rulesEditor.editor.setDisplayIndentGuides(true);
    // Verteilung.rulesEditor.editor.commands.addCommand({
    //     name: "save",
    //     bindKey: {
    //         win: "Ctrl-Shift-S",
    //         mac: "Command-s"
    //     },
    //     exec: save
    // });
    Verteilung.rulesEditor.editor.commands.addCommand({
        name: "format",
        bindKey: {
            win: "Ctrl-Shift-F",
            mac: "Command-f"
        },
        exec: format
    });
    Verteilung.rulesEditor.editor.$blockScrolling = Infinity;
    Verteilung.rulesEditor.editor.getSession().on('change', function() {
        if (Verteilung.rulesEditor.editor.getSession().getValue().length > 0) {
            verteilungRulesEditMenu.superfish('enableItem', 'editMenuVerteilungRulesEdit');
            if (alfrescoServerAvailable)
                verteilungRulesActionMenu.superfish('enableItem', 'actionMenuVerteilungRulesUpload');
        } else {
            verteilungRulesEditMenu.superfish('disableItem', 'editMenuVerteilungRulesEdit');
            verteilungRulesActionMenu.superfish('disableItem', 'actionMenuVerteilungRulesUpload');
        }
    });
    Verteilung.rulesEditor.fontsize = Verteilung.rulesEditor.editor.getFontSize();
    
    Verteilung.textEditor.editor = ace.edit("inTxt");
    Verteilung.textEditor.editor.setTheme("ace/theme/chrome");
    Verteilung.textEditor.editor.setShowInvisibles(true);
    Verteilung.textEditor.editor.setShowPrintMargin(false);
    Verteilung.textEditor.editor.getSession().setMode("ace/mode/text");
    Verteilung.textEditor.editor.$blockScrolling = Infinity;
    Verteilung.textEditor.fontsize = Verteilung.textEditor.editor.getFontSize();
    Verteilung.textEditor.editor.getSession().on('change', function(e) {
        if (Verteilung.textEditor.editor.getSession().getValue().length > 0) {
            verteilungTxtEditMenu.superfish('enableItem', 'editMenuVerteilungTxtEdit');
        } else {
            verteilungTxtEditMenu.superfish('disableItem', 'editMenuVerteilungTxtEdit');
        }
        if (Verteilung.textEditor.scriptMode) {
            if (Verteilung.textEditor.editor.getSession().getValue().length > 0) {
                verteilungTxtActionMenu.superfish('enableItem', 'actionMenuVerteilungTxtScriptReload');
                verteilungTxtActionMenu.superfish('enableItem', 'actionMenuVerteilungTxtScriptUpload');
            } else {
                verteilungTxtActionMenu.superfish('disableItem', 'actionMenuVerteilungTxtScriptReload');
                verteilungTxtActionMenu.superfish('disableItem', 'actionMenuVerteilungTxtScriptUpload');

            }
        } else {
            if (Verteilung.textEditor.editor.getSession().getValue().length > 0) {
                verteilungTxtActionMenu.superfish('enableItem', 'actionMenuVerteilungTxtWork');
                if (Verteilung.textEditor.file)
                    verteilungTxtActionMenu.superfish('enableItem', 'actionMenuVerteilungTxtSendToInbox');
            } else {
                verteilungTxtActionMenu.superfish('disableItem', 'actionMenuVerteilungTxtSendToInbox');
            }
        }

    });
    let zone = document.getElementById('inTxt');
    zone.addEventListener('dragover', handleDragOver, false);
    zone.addEventListener('drop', handleFileSelect, false);
    createVerteilungMenus();
    loadVerteilungTable();

    handleVerteilungImageClicks();
    openRules();
    manageControls();
}

function createOutputMenus() {
    $('#actionMenuOutput').superfish({
        menuData: {
            actionMenuOutputAction: {
                title: "Aktion",
                className: "fas fa-sign-in-alt fa-1x",
                autoClose: true,
                actionMenuOutputClear: {
                    title: "Löschen",
                    className: "fas fa-trash-alt fa-1x",
                    disabled: false,
                    action: function () {
                        try {
                            Logger.clear();
                        } catch (e) {
                            errorHandler(e);
                        }
                    }
                },
                actionMenuOutputLevel: {
                    title: "Level",
                    className: "fas fa-thermometer-half fa-1x",
                    disabled: false,
                    actionMenuOutputLevelNone: {
                        title: "Nichts",
                        className: "fas fa-times fa-1x",
                        autoClose: true,
                        selected: Logger.getLevel() === Level.NONE,
                        action: function (event) {
                            try {
                                Logger.setLevel(Level.NONE);
                                event.data.root.superfish('selectItem', 'actionMenuOutputLevelNone');
                                fillMessageBox(true);
                            } catch (e) {
                                errorHandler(e);
                            }
                        }
                    },
                    actionMenuOutputLevelError: {
                        title: "Fehler",
                        className: "fas fa-ban fa-1x",
                        autoClose: true,
                        selected: Logger.getLevel() === Level.ERROR,
                        action: function (event) {
                            try {
                                Logger.setLevel(Level.ERROR);
                                event.data.root.superfish('selectItem', 'actionMenuOutputLevelError');
                                fillMessageBox(true);
                            } catch (e) {
                                errorHandler(e);
                            }
                        }
                    },
                    actionMenuOutputLevelWarn: {
                        title: "Warnungen",
                        className: "fas fa-exclamation-circle fa-1x",
                        autoClose: true,
                        selected: Logger.getLevel() === Level.WARN,
                        action: function (event) {
                            try {
                                Logger.setLevel(Level.WARN);
                                event.data.root.superfish('selectItem', 'actionMenuOutputLevelWarn');
                                fillMessageBox(true);
                            } catch (e) {
                                errorHandler(e);
                            }
                        }
                    },
                    actionMenuOutputLevelInfo: {
                        title: "Info",
                        className: "fas fa-info-circle fa-1x",
                        autoClose: true,
                        selected: Logger.getLevel() === Level.INFO,
                        action: function (event) {
                            try {
                                Logger.setLevel(Level.INFO);
                                event.data.root.superfish('selectItem', 'actionMenuOutputLevelInfo');
                                fillMessageBox(true);
                            } catch (e) {
                                errorHandler(e);
                            }
                        }
                    },
                    actionMenuOutputLevelDebug: {
                        title: "Debug",
                        className: "fas fa-bug fa-1x",
                        autoClose: true,
                        selected: Logger.getLevel() === Level.DEBUG,
                        action: function (event) {
                            try {
                                Logger.setLevel(Level.DEBUG);
                                event.data.root.superfish('selectItem', 'actionMenuOutputLevelDebug');
                                fillMessageBox(true);
                            } catch (e) {
                                errorHandler(e);
                            }
                        }
                    },
                    actionMenuOutputLevelTrace: {
                        title: "Trace",
                        className: "fas fa-truck fa-1x",
                        autoClose: true,
                        selected: Logger.getLevel() === Level.TRACE,
                        action: function (event) {
                            try {
                                Logger.setLevel(Level.TRACE);
                                event.data.root.superfish('selectItem', 'actionMenuOutputLevelTrace');
                                fillMessageBox(true);
                            } catch (e) {
                                errorHandler(e);
                            }
                        }
                    }
                }
            }
        }
    });

    editMenuOutput = $('#editMenuOutput').superfish({
        menuData: {
            editMenuOutputEdit: {
                title: "Editor",
                className: "far fa-edit fa-1x",
                autoClose: true,
                disabled: true,
                editMenuOutputSearch: {
                    title: "Suchen",
                    className: "fas fa-search fa-1x",
                    autoClose: true,
                    action: function () {
                        try {
                            Verteilung.outputEditor.editor.execCommand("find")
                        } catch (e) {
                            errorHandler(e);
                        }
                    }
                },
                editMenuOutputGutter: {
                    title: "Zeilennummern",
                    className: "fas fa-list-ol fa-1x",
                    autoClose: false,
                    selected: Verteilung.outputEditor.editor.renderer.getShowGutter(),
                    action: function (event) {
                        try {
                            const gutter = !Verteilung.outputEditor.editor.renderer.getShowGutter();
                            Verteilung.outputEditor.editor.renderer.setShowGutter(gutter);
                            event.data.root.superfish((gutter ? 'selectItem':'deselectItem'), 'editMenuOutputGutter');
                        } catch (e) {
                            errorHandler(e);
                        }
                    }
                }

            }
        }
    });
}


function createVerteilungMenus() {
    verteilungTxtActionMenu = $('#actionMenuVerteilungTxt').superfish({
        menuData: {
            actionMenuVerteilungTxtAction: {
                title: "Aktion",
                className: "fas fa-sign-in-alt fa-1x",
                autoClose: true,
                actionMenuVerteilungTxtWork: {
                    title: "Erkennung starten",
                    className: "fas fa-cog fa-1x",
                    disabled: true,
                    autoClose: true,
                    action: function () {
                        try {
                            work();
                        } catch (e) {
                            errorHandler(e);
                        }
                    }
                },
                actionMenuVerteilungTxtSendToInbox: {
                    title: "Inbox senden",
                    className: "fas fa-upload fa-1x",
                    disabled: true,
                    autoClose: true,
                    action: function () {
                        try {
                            sendToInbox();
                        } catch (e) {
                            errorHandler(e);
                        }
                    }
                },
                actionMenuVerteilungTxtPDF: {
                    title: "PDF anzeigen",
                    className: "far fa-file-pdf fa-1x",
                    disabled: true,
                    autoClose: true,
                    action: function () {
                        try {
                            const file = new Blob([Verteilung.textEditor.content.original], {type: "application/pdf"});
                            const fileURL = URL.createObjectURL(file);
                            window.open(fileURL);
                        } catch (e) {
                            errorHandler(e);
                        }
                    }
                },
                actionMenuVerteilungTxtScript: {
                    title: "Script anzeigen",
                    className: "fab fa-js fa-1x",
                    disabled: false,
                    autoClose: true,
                    action: function () {
                        try {
                            openScript();
                        } catch (e) {
                            errorHandler(e);
                        }
                    }
                },
                actionMenuVerteilungTxtScriptReload: {
                    title: "Script anwenden",
                    className: "fas fa-sync fa-1x",
                    removed: true,
                    autoClose: true,
                    action: function () {
                        try {
                            activateScriptToContext();
                        } catch (e) {
                            errorHandler(e);
                        }
                    }
                },
                actionMenuVerteilungTxtScriptDownload: {
                    title: "Script vom Server laden",
                    className: "fas fa-download fa-1x",
                    removed: true,
                    autoClose: true,
                    action: function () {
                        try {
                            getScript();
                        } catch (e) {
                            errorHandler(e);
                        }
                    }
                },
                actionMenuVerteilungTxtScriptUpload: {
                    title: "Script zum Server kopieren",
                    className: "fas fa-upload fa-1x",
                    removed: true,
                    autoClose: true,
                    action: function () {
                        try {
                            if(sendScript())
                                alertify.success("Script erfolgreich zum Server übertragen");
                        } catch (e) {
                            errorHandler(e);
                        }
                    }
                },
                actionMenuVerteilungTxtScriptClose: {
                    title: "Script schließen",
                    className: "far fa-window-close fa-1x",
                    removed: true,
                    autoClose: true,
                    action: function () {
                        try {
                            closeScript();
                        } catch (e) {
                            errorHandler(e);
                        }
                    }
                }



            }
        }
    });

    verteilungTxtEditMenu = $('#editMenuVerteilungTxt').superfish({
        menuData: {
            editMenuVerteilungTxtEdit: {
                title: "Editor",
                className: "far fa-edit fa-1x",
                autoClose: true,
                disabled: true,
                editMenuVerteilungTxtSearch: {
                    title: "Suchen",
                    className: "fas fa-search fa-1x",
                    autoClose: true,
                    action: function () {
                        try {
                            Verteilung.textEditor.editor.execCommand("find");
                        } catch (e) {
                            errorHandler(e);
                        }
                    }
                },
                editMenuVerteilungTxtScriptBeautify: {
                    title: "Script formatieren",
                    className: "fas fa-gavel fa-1x",
                    removed: true,
                    autoClose: true,
                    action: function () {
                        try {
                            formatScript();
                        } catch (e) {
                            errorHandler(e);
                        }
                    }
                },
                editMenuVerteilungTxtScriptGutter: {
                    title: "Zeilennummern",
                    className: "fas fa-list-ol fa-1x",
                    autoClose: false,
                    selected: Verteilung.textEditor.editor.renderer.getShowGutter(),
                    action: function (event) {
                        try {
                            const gutter = !Verteilung.textEditor.editor.renderer.getShowGutter();
                            Verteilung.textEditor.editor.renderer.setShowGutter(gutter);
                            event.data.root.superfish((gutter ? 'selectItem':'deselectItem'), 'editMenuVerteilungTxtScriptGutter');
                        } catch (e) {
                            errorHandler(e);
                        }
                    }
                }
            }
        }
    });

    verteilungRulesActionMenu = $('#actionMenuVerteilungRules').superfish({
        menuData: {
            actionMenuVerteilungRulesAction: {
                title: "Aktion",
                className: "fas fa-sign-in-alt fa-1x",
                autoClose: true,
                actionMenuVerteilungRulesDownload: {
                    title: "Regeln vom Server laden",
                    className: "fas fa-download fa-1x",
                    removed: true,
                    autoClose: true,
                    action: function () {
                        try {
                            getRules(window.parent.rulesID, false);
                        } catch (e) {
                            errorHandler(e);
                        }
                    }
                },
                actionMenuVerteilungRulesUpload: {
                    title: "Regeln zum Server kopieren",
                    className: "fas fa-upload fa-1x",
                    removed: true,
                    autoClose: true,
                    action: function () {
                        try {
                            if (sendRules())
                                alertify.success('Regeln erfolgreich zum Server übertragen');
                        } catch (e) {
                            errorHandler(e);
                        }
                    }
                }
            }
        }
    });

    verteilungRulesEditMenu = $('#editMenuVerteilungRules').superfish({
        menuData: {
            editMenuVerteilungRulesEdit: {
                title: "Editor",
                className: "far fa-edit fa-1x",
                autoClose: true,
                disabled: true,
                editMenuVerteilungRulesSearch: {
                    title: "Suchen",
                    className: "fas fa-search fa-1x",
                    autoClose: true,
                    action: function () {
                        try {
                            Verteilung.rulesEditor.editor.execCommand("find")
                        } catch (e) {
                            errorHandler(e);
                        }
                    }
                },
                editMenuVerteilungRulesFold: {
                    title: "Alles einklappen",
                    className: "fas fa-compress fa-1x",
                    autoClose: true,
                    action: function () {
                        try {
                            Verteilung.rulesEditor.editor.getSession().foldAll(1);
                        } catch (e) {
                            errorHandler(e);
                        }
                    }
                },
                editMenuVerteilungRulesUnfold: {
                    title: "Alles ausklappen",
                    className: "fas fa-expand fa-1x",
                    autoClose: true,
                    action: function () {
                        try {
                            Verteilung.rulesEditor.editor.getSession().unfold();
                        } catch (e) {
                            errorHandler(e);
                        }
                    }
                },
                editMenuVerteilungRulesBeautify: {
                    title: "Regeln formatieren",
                    className: "fas fa-gavel fa-1x",
                    autoClose: true,
                    action: function () {
                        try {
                            format();
                        } catch (e) {
                            errorHandler(e);
                        }
                    }
                },
                editMenuVerteilungRulesGutter: {
                    title: "Zeilennummern",
                    className: "fas fa-list-ol fa-1x",
                    autoClose: false,
                    selected: Verteilung.rulesEditor.editor.renderer.getShowGutter(),
                    action: function (event) {
                        try {
                            const gutter = !Verteilung.rulesEditor.editor.renderer.getShowGutter();
                            Verteilung.rulesEditor.editor.renderer.setShowGutter(gutter);
                            event.data.root.superfish((gutter ? 'selectItem':'deselectItem'), 'editMenuVerteilungRulesGutter');
                        } catch (e) {
                            errorHandler(e);
                        }
                    }
                }

            }
        }
    });
}

/**
 * startet die Anwendung
 */
function start() {
    try {
        Logger.setCallback(function() {
            if (Verteilung.outputEditor.editor)
                Verteilung.outputEditor.editor.getSession().setValue(Logger.getMessages(true));
        });
        $(document).tooltip();
        $('.ui-dialog-titlebar-close').tooltip('disable');
        $("#breadcrumb").jBreadCrumb();
        loadLayout();
        document.getElementById('filesinput').addEventListener('change', readMultiFile, false);
        REC.init();
         // Zahlenformat festlegen
        $.format.locale({
            number: {
                groupingSeparator: '.',
                decimalSeparator: ','
            }
        });
        initApplication();
        Verteilung.outputEditor.editor = ace.edit("inOutput");
        Verteilung.outputEditor.editor.setReadOnly(true);
        Verteilung.outputEditor.editor.setShowPrintMargin(false);
        Verteilung.outputEditor.editor.$blockScrolling = Infinity;
        Verteilung.outputEditor.fontsize = Verteilung.outputEditor.editor.getFontSize();
        Verteilung.outputEditor.editor.getSession().on('change', function() {
            if (Verteilung.outputEditor.editor.getSession().getValue().length > 0) {
                editMenuOutput.superfish('enableItem', 'editMenuOutputEdit');
             } else {
                editMenuOutput.superfish('disableItem', 'editMenuOutputEdit');
            }
        });
        createOutputMenus();
        $('#clientPage').css("display","block");
    } catch(e) {
        errorHandler(e);
    }
}

