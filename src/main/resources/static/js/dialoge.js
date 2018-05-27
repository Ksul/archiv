
/**
 * Öffnet den Einstellungsdialog für die Alfresco Server Settings
 * @param modal    boolean, der festlegt ob das Fenster modal sein soll
 */
function startSettingsDialog(modal) {
    try {

        const data =  {
                server: getSettings("server"),
                binding: getSettings("binding"),
                user: getSettings("user"),
                password: getSettings("password"),
                store: getSettings("store")
        };
        if (!data.store)
            data.store = false;

        // Einstellungen für den Settings Dialog
        const dialogSettings = { id: "settingsDialog",
            schema: {
                type: "object",
                properties: {
                    user: {
                        type: "string",
                        title: "Benutzer",
                        required: true
                    },
                    password: {
                        type: "string",
                        title: "Password",
                        required: true
                    },
                    server: {
                        type: "string",
                        title: "Server",
                        required: true,
                        pattern: "^(ht|f)tp(s?)\:\/\/[0-9a-zA-Z]([-.\w]*[0-9a-zA-Z])*(:(0-9)*)*(\/?)([a-zA-Z0-9\-‌​\.\?\,\'\/\\\+&amp;%\$#_]*)?$"
                    },
                    binding: {
                        type: "string",
                        title: "Binding",
                        required: true,
                        pattern: "^(ht|f)tp(s?)\:\/\/[0-9a-zA-Z]([-.\w]*[0-9a-zA-Z])*(:(0-9)*)*(\/?)([a-zA-Z0-9\-‌​\.\?\,\'\/\\\+&amp;%\$#_]*)?$"
                    },
                    store: {
                        type: "boolean",
                        title: "Einstellungen sichern",
                        required: false,
                        default: false
                    }
                }
            },
                options: {
                    renderForm: true,
                    form: {
                        buttons: {
                            submit: {value: "Sichern"},
                            reset: {value: "Abbrechen"}
                        }
                    },
                fields: {
                    server: {
                        size: 60,
                        placeholder: "z.B.: http://[host]:[port]/alfresco/",
                        events: {
                            change: function(){
                                if (!this.getValue().endsWith("/")) {
                                    this.setValue(this.getValue() + "/");
                                    this.refresh();
                                }
                            }
                        }
                    },
                    binding: {
                        size: 100,
                        placeholder: "z.B.: http://[host]:[port]/alfresco/api/-default-/public/cmis/versions/1.0/atom"
                    },
                    user: {
                        size: 30
                    },
                    password: {
                        type: "password",
                        size: 20
                    },
                    store : {

                    }
                }
            },

            view: {
                parent: "web-edit",
                locale: "de_DE",
                layout: {
                    template: "columnGridLayout",
                    bindings: {
                        server:    "server",
                        binding:   "binding",
                        user:      "user",
                        password:  "password",
                        store:     "store"
                    }
                },
                templates: {
                    columnGridLayout: '<div class="filter-content">' + '{{#if options.label}}<h2>{{options.label}}</h2><span></span>{{/if}}' + '{{#if options.helper}}<p>{{options.helper}}</p>{{/if}}'
                        + '<div id="server" class="col-1-1"> </div>'
                        + '<div id="binding" class="col-1-1"> </div>'
                        + '<div id="user" class="col-7-12"> </div><div id="password" class="col-5-12"> </div>'
                        + '<div id="store" class="col-1-1"> </div>'
                        + '</div>'                }

            },
            data: data,
            ui: "jquery-ui",

            postRender: function (renderedField) {
                try {
                    const server = renderedField.childrenByPropertyId["server"];
                    const binding = renderedField.childrenByPropertyId["binding"];
                    binding.subscribe(server, function () {
                        if (!this.getValue().trim().length)
                            this.setValue(server.data + "api/-default-/public/cmis/versions/1.0/atom");
                        this.refresh();
                    });
                    const form = renderedField.form;
                    if (form) {
                        form.registerSubmitHandler(function () {
                            if (form.isFormValid()) {
                                try {
                                    const input = $("#dialogBox").alpaca().getValue();
                                    if (!input.server.endsWith("/"))
                                        input.server = input.server + "/";
                                    settings = {
                                        settings: [
                                            {key: "server", value: input.server},
                                            {key: "user", value: input.user},
                                            {key: "password", value: input.password},
                                            {key: "binding", value: input.binding},
                                            {key: "store", value: input.store}
                                        ]
                                    };
                                    if (store) {
                                        $.cookie("settings", JSON.stringify(settings), {expires: 9999});
                                        Logger.log(Level.INFO, "Einstellungen gesichert");
                                    }
                                    closeDialog();
                                    initApplication();
                                    loadAlfrescoTree();
                                } catch (e) {
                                    errorHandler(e);
                                }
                            }
                        });
                    }

                } catch (e) {
                    errorHandler(e);
                }
            }
        };
        startDialog("Server Einstellungen", dialogSettings, 480, modal);
    } catch (e) {
        errorHandler(e);
    }
}


/**
 * startet den Detaildialog für Dokumente
 * @param data     die Daten welche bearbeitet werden sollen
 * @param modus    der Modus web-edit    Dokument editieren
 * @param modal    boolean, der festlegt ob das Fenster modal sein soll
 */
function startDocumentDialog(data, modus, modal) {

    try {
        // Konversion
        if (data.documentDate)
            data.documentDateDisplay = $.datepicker.formatDate("dd.mm.yy", new Date(Number(data.documentDate)));
        else
            data.documentDateDisplay = $.datepicker.formatDate("dd.mm.yy", new Date());
        if (data.amount)
            data.amountDisplay = $.format.number(data.amount, '#,##0.00');
        else
            data.amountDisplay = "";
        if (!data.tax)
            data.tax = false;
        if (!data.person)
            data.person = "Klaus";

        // Einstellungen für den Dokumentendialog
        const dialogDocumentDetailsSettings = { id: "detailDialog",
            schema: {
                type: "object",
                title: function() {
                    if (modus === "web-display")
                        return "Dokument löschen?";
                    else
                        return "a";
                },
                properties: {
                    name: {
                        type: "string",
                        title: "Dateiname",
                        required: false
                    },
                    title: {
                        type: "string",
                        title: "Titel",
                        required: true
                    },

                    description: {
                        type: "string",
                        title: "Beschreibung",
                        required: false
                    },
                    person: {
                        type: "string",
                        title: "Person",
                        enum: [
                            "Klaus",
                            "Katja",
                            "Till",
                            "Kilian"
                        ],
                        required: true,
                        default: "Klaus"
                    },
                    amountDisplay: {
                        type: "string",
                        required: false,
                        properties: {}
                    },
                    documentDateDisplay: {
                        type: "string",
                        title: "Datum",
                        format: "date",
                        required: true
                    },
                    idvalue: {
                        type: "string",
                        title: "Id",
                        required: false
                    },
                    tax: {
                        type: "boolean",
                        title: "Steuern",
                        required: false
                    }

                }
            },
            options: {
                renderForm: true,
                form: {
                    buttons: function () {
                        switch (true) {
                            case /display/.test(modus):
                                return {
                                    delete: {
                                        type: "button",
                                        styles: " ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only ",
                                        value: "Löschen"
                                    },
                                    reset: {value: "Abbrechen"}
                                };
                            case /create/.test(modus):
                                return {
                                    submit: {value: "Erstellen"},
                                    reset: {value: "Abbrechen"}
                                };
                            default:
                                return {
                                    submit: {value: "Sichern"},
                                    reset: {value: "Abbrechen"}
                                };
                        }
                    }
                },
                fields: {
                    title: {
                        size: 30,
                        focus: true,
                        typeahead: {
                            config: {
                                autoselect: true,
                                highlight: true,
                                hint: true,
                                minLength: 3
                            },
                            datasets: {
                                type: "local",
                                source: function() {
                                    const results = [];
                                    const json = executeService({name: "getTitles", ignoreError: true});
                                    for (let i = 0; i < json.data.length; i++) {
                                        results.push({
                                            value: json.data[i]
                                        });
                                    }
                                    return results;
                                }
                            }
                        }
                    },
                    name: {
                        size: 30,
                        readonly: true
                    },
                    description: {
                        type: "textarea",
                        size: 60
                    },
                    person: {
                        type: "select",
                        hideInitValidationError: true,
                        emptySelectFirst: true
                    },
                    amountDisplay: {
                        type: "currency",
                        label: "Betrag",
                        centsLimit: 2,
                        centsSeparator: ",",
                        prefix: "",
                        round: "none",
                        thousandsSeparator: ".",
                        suffix: "",
                        unmask: true,
                        allowNegative : true,
                        helpers: [],
                        validate: true,
                        disabled: false,
                        showMessages: true,
                        renderButtons: true,
                        data: {},
                        attributes: {},
                        allowOptionalEmpty: true,
                        autocomplete: false,
                        disallowEmptySpaces: false,
                        disallowOnlyEmptySpaces: false,
                        fields: {}
                    },
                    tax: {
                        rightLabel: "relevant"
                    },
                    documentDateDisplay: {
                        type: "date",
                        label: "Dokumentdatum",
                        helpers: [],
                        validate: true,
                        disabled: false,
                        showMessages: true,
                        renderButtons: true,
                        allowOptionalEmpty: true,
                        autocomplete: false,
                        disallowEmptySpaces: false,
                        disallowOnlyEmptySpaces: false,
                        dateFormatRegex: "/(0[1-9]|[12][0-9]|3[01])\.(0[1-9]|1[012])\.(19|20)\d\d$/",
                        picker: {
                            useCurrent: true,
                            format: "DD.MM.YYYY",
                            locale: "de_DE",
                            dayViewHeaderFormat: "DD.MM.YYYY",
                            extraFormats: []
                        },
                        dateFormat: "DD.MM.YYYY",
                        manualEntry: true
                    }
                }
            },
            view: {
                parent: modus,
                locale: "de_DE",
                layout: {
                    template: "threeColumnGridLayout",
                    bindings: {
                        name: "column-1-1",
                        title: "column-1-1",
                        description: "column-1-1",
                        person: "column-1-2",
                        documentDateDisplay: "column-2-2",
                        amountDisplay: "column-1-2",
                        idvalue: "column-2-2",
                        tax: "column-1-b"

                    }
                },
                templates: {
                    threeColumnGridLayout: '<div class="filter-content">'
                        + '<div id="column-1-1" class="col-1-1"> </div>'
                        + '<div id="column-1-2" class="col-1-2"> </div> <div id="column-2-2" class="col-1-2"> </div>'
                        + '<div id="column-1-7_12" class="col-7-12"> </div> <div id="column-2-5_12" class="col-5-12"> </div>'
                        + '<div id="column-1-3" class="col-1-3"> </div> <div id="column-2-3" class="col-1-3"> </div> <div id="column-3-3" class="col-1-3"> </div>'
                        + '<div id="column-1-b" class="col-1-1"> </div>'
                        + '</div>'
                }

            },
            data: data,
            ui: "jquery-ui",
            postRender: function (renderedField) {
                const form = renderedField.form;
                if (form) {
                    form.registerSubmitHandler(function () {
                        if (form.isFormValid()) {
                            try {
                                const alpaca = $("#dialogBox").alpaca();
                                // Werte übertragen
                                const input = alpaca.getValue();
                                // die original Daten sichern.
                                const origData = alpaca.data;
                                if (modus === "web-edit") {
                                    // Konvertierung
                                    if (input.amountDisplay && typeof input.amountDisplay === "string")
                                        input.amount = parseFloat(input.amountDisplay.replace(/\./g, '').replace(/,/g, "."));
                                    if (input.documentDateDisplay && typeof input.documentDateDisplay === "string")
                                        input.documentDate = $.datepicker.parseDate("dd.mm.yy", input.documentDateDisplay).getTime();
                                    // Wurde was geändert?
                                    if ((input.title && origData.title !== input.title) ||
                                        (input.description && origData.description !== input.description) ||
                                        (input.person && origData.person !== input.person) ||
                                        (input.documentDate && origData.documentDate !== input.documentDate) ||
                                        (input.amount && origData.amount !== input.amount) ||
                                        (input.tax !== origData.tax)) {
                                        editDocument(input, origData.objectID);
                                    }
                                } else if (modus === "web-display") {
                                    deleteDocument(origData);
                                } else if (modus === "web-create") {
                                    createDocument(alpaca.getValue(), alpaca.data.file);
                                }
                                closeDialog();
                            } catch (e) {
                                errorHandler(e);
                            }
                        }
                    });
                }
            }
        };
        const additionalButton =[{id:".alpaca-form-button-delete", function: deleteDocument }];
        startDialog(modus === "web-display" ? "Dokument löschen?" : "Dokument Eigenschaften", dialogDocumentDetailsSettings, 450, modal, additionalButton);
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * startet den Detaildialog für Folder
 * @param data     die Daten welche bearbeitet werden sollen
 * @param modus    der Modus web-create     neuen Ordner erzeugen
 *                           web-edit       Ordner editieren
 *                           web-display    Ordner löschen
 * @param modal    boolean, der festlegt ob das Fenster modal sein soll                          
 */
function startFolderDialog(data, modus, modal) {
    
    try {

        // Einstellungen für den Folder Dialog
        const folderDialogSettings = { id: "detailDialog",
            schema: {
                type: "object",
                properties: {
                    name: {
                        type: "string",
                        title: "Name",
                        required: true,
                        readonly: !(data.objectID !== alfrescoRootFolderId &&
                        data.objectID !== archivFolderId &&
                        data.objectID !== fehlerFolderId &&
                        data.objectID !== unknownFolderId &&
                        data.objectID !== doubleFolderId &&
                        data.objectID !== inboxFolderId)
                    },
                    title: {
                        type: "string",
                        title: "Titel",
                        required: false
                    },
                    description: {
                        type: "string",
                        title: "Beschreibung",
                        required: false
                    }
                }
            },
            options: {
                renderForm: true,
                form: {
                    buttons: function () {
                        switch (true) {
                            case /display/.test(modus):
                                return {
                                    delete: {
                                        type: "button",
                                        styles: "btn btn-primary",
                                        value: "Löschen"
                                    },
                                    reset: {value: "Abbrechen"}
                                };
                            case /create/.test(modus):
                                return {
                                    submit: {value: "Erstellen"},
                                    reset: {value: "Abbrechen"}
                                };
                            default:
                                return {
                                    submit: {value: "Sichern"},
                                    reset: {value: "Abbrechen"}
                                };
                        }
                    }
                },
                fields: {

                    name: {
                        size: 30
                    },
                    title: {
                        size: 30
                    },
                    description: {
                        type: "textarea",
                        size: 150
                    }
                }
            },
            data: data,
            view: {
                parent: modus,
                locale: "de_DE",
                layout: {
                    template: "threeColumnGridLayout",
                    bindings: {
                        name: "column-1-1",
                        title: "column-1-1",
                        description: "column-1-1"

                    }
                },
                templates: {
                    threeColumnGridLayout: '<div class="filter-content">'
                        + '<div id="column-1-1" class="col-1-1"> </div>'
                        + '<div id="column-1-2" class="col-1-2"> </div> <div id="column-2-2" class="col-1-2"> </div>'
                        + '<div id="column-1-7_12" class="col-7-12"> </div> <div id="column-2-5_12" class="col-5-12"> </div>'
                        + '<div id="column-1-3" class="col-1-3"> </div> <div id="column-2-3" class="col-1-3"> </div> <div id="column-3-3" class="col-1-3"> </div>'
                        + '</div>'
                }

            },
            ui: "bootstrap",
            postRender: function (renderedField) {

                const form = renderedField.form;
                if (form) {
                    form.registerSubmitHandler(function () {
                        if (form.isFormValid()) {
                            try {
                                const alpaca = $("#dialogBox").alpaca();
                                // Werte übertragen
                                const input = alpaca.getValue();
                                // die original Daten sichern.
                                const origData = alpaca.data;
                                if (modus === "web-create") {
                                    // ein neuer Ordner wird erstellt
                                    createFolder(input, origData);
                                    
                                }
                                else if (modus === "web-edit") {
                                    // bestehender Ordner wird editiert
                                    if ((input.name && input.name !== origData.name) ||
                                        (input.title && input.title !== origData.title) ||
                                        (input.description && input.description !== origData.description)) {
                                        const erg = editFolder(input, origData.objectID);
                                        if (!erg.success)
                                            alertify.alert("Fehler", erg.error);
                                    }
                                }
                                closeDialog();
                            } catch (e) {
                                errorHandler(e);
                            }
                        }
                    });
                }
            }
        };
        const additionalButton = [{id: ".alpaca-form-button-delete", function: deleteFolder}];
        startDialog(modus === "web-display" ? "Ordner löschen?" : "Ordner Eigenschaften", folderDialogSettings, 460, modal, additionalButton);
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * startet den Detaildialog für Kommentare
 */
function startCommentsDialog(comments) {
    try {
        const data = comments.items;

        $dialog = $('<div> <table cellpadding="0" cellspacing="0" border="0" class="display" id="custTabelle"></table> </div>').dialog({
            autoOpen: false,
            title: "Kommentare",
            modal: true,
            height:300,
            width:800,
            buttons: {
                Ok: function () {
                    $(this).dialog("destroy");
                }
            }
        }).css({height:"300px", width:"800px", overflow:"auto"});

        $dialog.dialog('open');
        $('#custTabelle').DataTable({
            jQueryUI: true,
            paging: false,
            data: data,
            scrollX: "100%",
            scrollXInner: "100%",
            // "sScrollY" : calcDataTableHeight(),
            autoWidth: true,
            lengthChange: false,
            searching: false,
            columns: [
                {
                    data: "author.username",
                    title: "User",
                    defaultContent: '',
                    type: "string",
                    width: "120px",
                    class: "alignLeft"
                },
                {
                    data: "modifiedOn",
                    title: "Datum",
                    defaultContent: '',
                    type: "string",
                    width: "120px",
                    class: "alignLeft"
                },
                {
                    title: "Kommentar",
                    data: "content",
                    class: "alignLeft"
                }
            ],
            columnDefs: [
                {
                    targets: [0, 2],
                    visible: true
                },
                {
                    targets: [1],
                    visible: true,
                    render: function (data, type, row) {
                        return  $.formatDateTime('dd.mm.yy hh:ii:ss', new Date(Date.parse(row.modifiedOnISO)));
                    }
                }
            ],
            info: false
        });
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * startet den Dialog zum Hochladen
 */
function startUploadDialog() {
    try {
        const data = comments.items;

        const $dialog = $('<div> <input type="file" multiple="" name="files[]" class="dnd-file-selection-button"> </div>').dialog({
            autoOpen: false,
            title: "Kommentare",
            modal: true,
            height:300,
            width:800,
            buttons: {
                Ok: function () {
                    $(this).dialog("destroy");
                }
            }
        }).css({height:"300px", width:"800px", overflow:"auto"});

        $dialog.dialog('open');
        $('#custTabelle').DataTable({
            jQueryUI: true,
            paging: false,
            data: data,
            scrollX: "100%",
            scrollXInner: "100%",
            // "sScrollY" : calcDataTableHeight(),
            autoWidth: true,
            lengthChange: false,
            searching: false,
            columns: [
                {
                    data: "author.username",
                    title: "User",
                    defaultContent: '',
                    type: "string",
                    width: "120px",
                    class: "alignLeft"
                },
                {
                    data: "modifiedOn",
                    title: "Datum",
                    defaultContent: '',
                    type: "string",
                    width: "120px",
                    class: "alignLeft"
                },
                {
                    title: "Kommentar",
                    data: "content",
                    class: "alignLeft"
                }
            ],
            columnDefs: [
                {
                    targets: [0, 2],
                    visible: true
                },
                {
                    targets: [1],
                    visible: true,
                    render: function (data, type, row) {
                        return  $.formatDateTime('dd.mm.yy hh:ii:ss', new Date(Date.parse(row.modifiedOnISO)));
                    }
                }
            ],
            info: false
        });
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * startet den Dialog zum Verschieben von Dokumenten
 * @param  rowData  Array mit den Tabellendaten
 * @param  table    die Tabelle
 * @param  typ      Typ 'Dokumente' oder 'Ordner'
 *
 */
function startMoveDialog(rowData, table, typ) {

    try {
        if (!typ)
            typ = "Dokumente";
        
        const moveDialogSettings = { id: "moveDialog",
            schema: {
                type: "object",
                properties: {
                    token: {
                        type: "string"                    }
                }
            },
            options: {
                renderForm: true,
                fields: {
                    token: {
                        type: "hidden"
                    }
                },
                form: {
                    buttons: {
                        submit: {value: "Verschieben"},
                        reset: {value: "Abbrechen"}
                    },
                    toggleSubmitValidState: true
                }
            },
            view: {
                parent: "web-edit",
                locale: "de_DE",
                layout: {
                    template: "layout"
                },
                templates: {
                    layout: '<div class="filter-content" id="dialogTree"></div>'
                }
            },
            ui: "jquery-ui",
            postRender: function (renderedField) {
                const form = renderedField.form;
                form.disableSubmitButton();
                $("#dialogTree").jstree({
                    core: {
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
                        themes: {
                            responsive: true,
                            variant: 'big',
                            stripes: false,
                            dots: true,
                            icons: true
                        }
                    },
                    types : {
                        "#" : {
                            max_children : 1
                        },
                        archivRootStandard : {
                            valid_children : ["archivFolderStandard", "archivDocumentFolderStandard", "archivFehlerFolderStandard"],
                            icon: "fa fa-file-text-o fa-15x awesomeEntity"
                        },
                        archivFolderStandard : {
                            valid_children : [],
                            icon: "fa fa-file-text-o fa-15x awesomeEntity"
                        },
                        archivDoubleFolderStandard : {
                            valid_children : [],
                            icon: "fa fa-file-text-o fa-15x awesomeEntity"
                        },
                        archivFehlerFolderStandard : {
                            valid_children : ["archivDoubleFolderStandard"],
                            icon: "fa fa-file-text-o fa-15x awesomeEntity"
                        },
                        archivDocumentFolderStandard : {
                            valid_children : ["documentFolderStandard"] ,
                            icon: "fa fa-file-text-o fa-15x awesomeEntity"
                        },
                        documentFolderStandard : {
                            valid_children : -1,
                            icon: "fa fa-file-text-o fa-15x awesomeEntity"
                        }
                    },
                    'plugins': ["dnd", "types"]
                }).on("changed.jstree",  function (event, data){
                    try {
                        if (data.action === "select_node") {
                           
                            if (data.node.data &&
                            data.node.data.objectID !== alfrescoRootFolderId &&
                            data.node.data.objectID !== archivFolderId &&
                            data.node.data.objectID !== fehlerFolderId &&
                            data.node.data.objectID !== unknownFolderId &&
                            data.node.data.objectID !== doubleFolderId &&
                                (data.node.data.objectID !== documentFolderId || typ === "Ordner") &&
                            rowData.filter(function(row){ return row.parentId ===  data.node.data.objectID }).length === 0) {
                                form.enableSubmitButton();
                            }
                            else {
                                form.disableSubmitButton();
                                data.instance.deselect_node(data.node, true);
                            }
                            data.instance.open_node(data.node);
                        }
                    } catch (e) {
                        errorHandler(e);
                    }
                });

                if (form) {
                    form.registerSubmitHandler(function () {
                        if (form.isFormValid()) {

                            try {
                                const dialogTree = $.jstree.reference('#dialogTree');
                                const nodeIds = dialogTree.get_selected();
                                // über alle selektierten Zeilen iterieren
                                for (let index = 0; index < rowData.length; ++index) {
                                    const done = function (json) {
                                        if (json.success) {
                                            const newData = json.data;
                                            const source = json.source;
                                            const target = json.target;
                                            Logger.log(Level.INFO, typ + " " + newData.name + " von " + source.path + " nach " + target.path + " verschoben");
                                            // nur wenn die Tabelle angeben ist werden die Zeilen gelöscht. Bei der Tabelle mit den Suchergebnissen
                                            // werden keine Einträge gelöscht und deshalb sollte die Tabelle dort auch nicht für diese Funktion mit
                                            // angegeben werden.
                                            if (table) {
                                                table.row('#' + newData.objectID).remove();
                                                table.draw();
                                            }
                                        }
                                    };
                                    // Verschieben....
                                    executeService({
                                        name: "moveNode",
                                        callback: done,
                                        errorMessage: typ + " konnte nicht verschoben werden:"
                                    }, [
                                        {name: "documentId", value: rowData[index].objectID},
                                        {name: "currentLocationId", value: rowData[index].parentId},
                                        {name: "destinationId", value: nodeIds[0]}
                                    ]);
                                }

                                closeDialog();
                            } catch (e) {
                                errorHandler(e);
                            }
                        }
                    });
                }
            }
        };


        startDialog(typ + " verschieben", moveDialogSettings, 400, true);


    } catch (e) {
        errorHandler(e);
    }
}

/**
 * schliesst den Dialog
 */
function closeDialog() {
    const dialogBox = $('#dialogBox');
    if (dialogBox) {
        dialogBox.dialog("close");
        dialogBox.remove();
    }
}

/**
 * startet den eigentlichen Dialog
 * @param title                     der Dialogtitel
 * @param dialogSettings            die Settings für den Dialog
 * @param width                     die Weite des Fensters
 * @param modal                     boolean der festlegt, ob das Fenster modal sein soll
 * @param callbacks                 Array mit Callbacks für weitere Buttons
 */
function startDialog(title, dialogSettings, width, modal, callbacks) {

    $("<div>", {id: "dialogBox", class: "grid gridpad"}).appendTo("body");
    $('#dialogBox').alpaca(dialogSettings).dialog({
        autoOpen: true,
        width: width,
        height: 'auto',
        modal: modal,
        title: title,
        dialogClass: "no-close",
        position: {
            my: "top",
            at: "center center-20%",
            of: window,
            collision: "fit",
            // Ensure the titlebar is always visible
            using: function (pos) {
                const topOffset = $(this).css(pos).offset().top;
                if (topOffset < 0) {
                    $(this).css("top", pos.top - topOffset);
                }
            }
        },
        open: function () {
            $(".alpaca-form-buttons-container").addClass("ui-dialog-buttonpane ui-widget-content");
            $(".alpaca-form-button-submit").button();
            $(".alpaca-form-button-reset").button().click(function () {
                closeDialog();
            });
            if (callbacks) {
                for (let i = 0; i < callbacks.length; i++) {
                    const obj = callbacks[i];
                    $(obj.id).button().click(function () {
                        obj.function($("#dialogBox").alpaca().data);
                        closeDialog();
                    });
                }
            }
        }
    });
}