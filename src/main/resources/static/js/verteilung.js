const PDF = "application/pdf";
/**
 * beschreibt die Position eines gefundenem Wertes in dem Dokument
 */
var Position = function() {
    this.typ = arguments[0];
    if (arguments.length === 7) {
        this.startPosition = eval(this.typ.editor).getSession().getValue().split("\n", arguments[1]).join("\n").length + arguments[2];
        this.endPosition = eval(this.typ.editor).getSession().getValue().split("\n", arguments[3]).join("\n").length + arguments[4];
        this.css = arguments[5];
        this.desc = arguments[6];
        
    } else {
        this.startPosition = arguments[1];
        this.endPosition = arguments[2];
        this.css = arguments[3];
        this.desc = arguments[4];
    }

    this.startRow = 0;
    this.startColumn = 0;
    this.endRow = 0;
    this.endColumn = 0;

    Verteilung.positions.add(this);



    this.getEditor = function() {
        return this.typ.editor;
    };

    this.getCSS = function() {
        return this.css;
    };

    this.getDesc = function(){
        return this.desc;
    };

    this.getStartRow = function() {
        return this.startRow;
    };

    this.getStartColumn = function() {
        return this.startColumn;
    };

    this.getEndRow = function() {
        return this.endRow;
    };

    this.getEndColumn = function() {
        return this.endColumn;
    };

    this.setStartRow = function(value) {
        this.startRow = value;
    };

    this.setStartColumn = function(value) {
        this.startColumn = value;
    };

    this.setEndRow = function(value) {
        this.endRow = value;
    };

    this.setEndColumn = function(value) {
        this.endColumn = value;
    };

    this.convertPosition = function ( text) {
        if (arguments.length === 0)
            text = eval(this.typ.editor).getSession().getValue();
        this.startRow = text.substring(0, this.startPosition).split(RETURN).length - 1;
        this.startColumn = this.startPosition - text.substring(0, this.startPosition).lastIndexOf(RETURN) - RETURN.length;
        this.endRow = text.substring(0, this.endPosition).split(RETURN).length - 1;
        this.endColumn = this.endPosition - text.substring(0, this.endPosition).lastIndexOf(RETURN) - RETURN.length;
        return this;
    };
};

function PositionContainer() {}

PositionContainer.prototype = [];

PositionContainer.prototype.add = function (pos) {
    var found = false;
    if (!(pos.startPosition === pos.endPosition) ) {
        for (var i = 0; i < this.length; i++) {
            if (pos.typ === this[i].typ && (pos.startPosition > this[i].startPosition && pos.endPosition < this[i].endPosition)) {
                this[i] = pos;
                found = true;
                break;
            }
        }
        if (!found)
            this.push(pos);
    }
};

PositionContainer.prototype.clear = function () {
    this.splice(0, this.length);
};

/**
 * gibt die Position für einen bestimmten Namen zurück
 * @param name    der Name
 * @returns {*}
 */
PositionContainer.prototype.get = function(name) {
    for (var i = 0; i < this.length; i++) {
        if (this[i].desc === name)
            return this[i];
    }
    return null;
};

/**
 * markiert in den Regeln die verwendeten Stellen
 */
PositionContainer.prototype.setMarkers = function () {

    var mark = function (position) {
        var p = position.convertPosition();
        var r = new Verteilung.Range(p.startRow, p.startColumn, p.endRow, p.endColumn);
        eval(p.typ.editor).getSession().addMarker(r, p.css, p.desc, false);
    };

    for (var i = 0; i < this.length; i++) {
        mark(this[i]);
    }
};


/**
 * zeigt die Progressbar
 * wird momentan nicht verwendet
 */
function showProgress() {
    $(function() {
        var progressbar = $("#progressbar"), progressLabel = $(".progress-label");
        progressbar.progressbar({
            value : 0,
            change : function() {
                progressLabel.text(Math.round(progressbar.progressbar("value")) + "%");
            },
            complete : function() {
                setTimeout(function(){progressLabel.text("");progressbar.progressbar("destroy");}, 3000);
            }
        });
    });
}

/**
 * verwaltet die Controls
 */
function manageControls() {

    document.getElementById('dtable').style.display = 'none';
    document.getElementById('verteilungTableFooter').style.display = 'none';
    document.getElementById('inTxt').style.display = 'block';
    document.getElementById('filesinput').style.display = 'block';
    document.getElementById('settings').style.display = 'block';


    if (multiMode) {
        document.getElementById('inTxt').style.display = 'none';
        document.getElementById('dtable').style.display = 'block';
        document.getElementById('verteilungTableFooter').style.display = 'block';
        verteilungTxtActionMenu.superfish('disableItem', 'actionMenuVerteilungTxtSendToInbox');
    }
    if (showMulti) {
        document.getElementById('dtable').style.display = 'none';
        document.getElementById('verteilungTableFooter').style.display = 'none';
    }
    if (Verteilung.textEditor.typ === PDF)
        verteilungTxtActionMenu.superfish('enableItem', 'actionMenuVerteilungTxtPDF');
    else
        verteilungTxtActionMenu.superfish('disableItem', 'actionMenuVerteilungTxtPDF');

    if (!alfrescoServerAvailable) {
        verteilungTxtActionMenu.superfish('disableItem', 'actionMenuVerteilungTxtScriptUpload');
        verteilungTxtActionMenu.superfish('disableItem', 'actionMenuVerteilungTxtScriptDownload');
        verteilungRulesActionMenu.superfish('disableItem', 'actionMenuVerteilungRulesDownload');
        verteilungRulesActionMenu.superfish('disableItem', 'actionMenuVerteilungRulesUpload');
        verteilungTxtActionMenu.superfish('disableItem', 'actionMenuVerteilungTxtSendToInbox');
    } else {
        verteilungRulesActionMenu.superfish('enableItem', 'actionMenuVerteilungRulesDownload');
        verteilungRulesActionMenu.superfish('enableItem', 'actionMenuVerteilungRulesUpload');
    }


    // Muss als letztes stehen
    if (Verteilung.textEditor.scriptMode) {
        //document.getElementById('tree').style.display = 'none';
        document.getElementById('dtable').style.display = 'none';
        document.getElementById('verteilungTableFooter').style.display = 'none';
        document.getElementById('inTxt').style.display = 'block';
        document.getElementById('filesinput').style.display = 'none';
        verteilungTxtActionMenu.superfish('hideItem', 'actionMenuVerteilungTxtWork');
        verteilungTxtActionMenu.superfish('hideItem', 'actionMenuVerteilungTxtPDF');
        verteilungTxtActionMenu.superfish('hideItem', 'actionMenuVerteilungTxtScript');
        verteilungTxtActionMenu.superfish('enableItem', 'actionMenuVerteilungTxtScriptReload');
        verteilungTxtActionMenu.superfish('enableItem', 'actionMenuVerteilungTxtScriptUpload');
        verteilungTxtActionMenu.superfish('enableItem', 'actionMenuVerteilungTxtScriptClose');
        verteilungTxtEditMenu.superfish('enableItem', 'editMenuVerteilungTxtScriptBeautify');
        verteilungTxtActionMenu.superfish('hideItem', 'actionMenuVerteilungTxtSendToInbox');
        verteilungTxtActionMenu.superfish('enableItem', 'actionMenuVerteilungTxtScriptDownload');

    } else {
        verteilungTxtActionMenu.superfish('enableItem', 'actionMenuVerteilungTxtScript');
        verteilungTxtActionMenu.superfish('hideItem', 'actionMenuVerteilungTxtScriptReload');
        verteilungTxtActionMenu.superfish('hideItem', 'actionMenuVerteilungTxtScriptUpload');
        verteilungTxtActionMenu.superfish('hideItem', 'actionMenuVerteilungTxtScriptDownload');
        verteilungTxtActionMenu.superfish('hideItem', 'actionMenuVerteilungTxtScriptClose');
        verteilungTxtEditMenu.superfish('hideItem', 'editMenuVerteilungTxtScriptBeautify');

    }
}


/**
 * lädt einen Text
 * @param content    der originale Inhalt der Datei
 * @param txt        der Inhalt des Dokumentes in Textform
 * @param name       der Name des Dokumentes
 * @param typ        der Dokumenttyp  (wird der eigentlich noch gebraucht)
 * @param container  ???
 */
function loadText(content, txt, name, typ, container) {
    try {
        multiMode = false;
        txt = txt.replace(/\r\n/g,'\n');
        Verteilung.textEditor.file = name;
        Verteilung.textEditor.typ = typ;
        Verteilung.textEditor.content.original = content;
        Verteilung.textEditor.content.asText = txt;
        currentContainer = container;
        $.each(Verteilung.textEditor.editor.getSession().getMarkers(false), function(element, index) {Verteilung.textEditor.editor.getSession().removeMarker(element)});
        $.each(Verteilung.rulesEditor.editor.getSession().getMarkers(false), function(element, index) {Verteilung.rulesEditor.editor.getSession().removeMarker(element)});
        $.each(Verteilung.propsEditor.editor.getSession().getMarkers(false), function(element, index) {Verteilung.propsEditor.editor.getSession().removeMarker(element)});
        for (let j = 0; j< Verteilung.rulesEditor.editor.getSession().getLength(); j++)
            Verteilung.rulesEditor.editor.getSession().removeGutterDecoration(j, "ace_selectXML");
        Verteilung.textEditor.editor.getSession().setValue(txt);
        document.getElementById('headerWest').textContent = name;
        Verteilung.propsEditor.editor.getSession().setValue("");
        manageControls();
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * lädt ein Dokument und trägt die Inhalte in die Tabelle ein
 * Methode wird benutzt wenn mehr als ein Dokument geladen werden soll
 * @param content           der originale Inhalt der Datei
 * @param txt               Textinhalt des Dokumentes
 * @param name              Name des Dokumentes
 * @param typ               Typ des Dokumentes
 * @param notDeleteable     Merker, ob das Dokument gelöscht werden kann (geht nur bei lokalen)
 * @param container         ???
 */
function loadMultiText(content, txt, name, typ,  notDeleteable, container) {
    try {
        multiMode = true;
        txt = txt.replace(/\r\n/g,'\n');
        var dat = [];
        REC.currentDocument.properties.content.write(txt);
        REC.currentDocument.name = name;
        REC.testRules(Verteilung.rulesEditor.editor.getSession().getValue());
        dat["text"] = txt;
        dat["file"] = name;
        dat["content"] = content;
        dat["log"] = Logger.getMessages(false, Level.TRACE);
        dat["result"] = REC.results;
        dat["position"] = Verteilung.positions;
        dat["xml"] = REC.currXMLName;
        dat["typ"] = typ;
        dat["error"] = REC.errors;
        dat["container"] = container;
        dat["notDeleteable"] = notDeleteable;
        daten[name] = dat;
        var ergebnis = [];
        ergebnis["error"] = REC.errors.length > 0;
        var row = [ null,name,  REC.currXMLName.join(" : "), ergebnis, uuid(), REC.errors ];
        tabelle.row.add(row).draw();
        manageControls();
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * handelt den File-Select der Dateiauswahl
 * @param evt    das Event
 */
function handleFileSelect(evt) {
    evt.stopPropagation();
    evt.preventDefault();
    const files = evt.dataTransfer.files;
    readFiles(files);
}

/**
 * handelt das Verhalten, wenn eine Datei Über den Bereich fallen gelassen wird
 * @param evt    das Event
 */
function handleDragOver(evt) {
    evt.stopPropagation();
    evt.preventDefault();
    evt.dataTransfer.dropEffect = 'copy';
}

/**
 * Eventlistener für den Dateiauswahldialog, wenn mehrere Dateien ausgewählt worden sind
 * @param evt    das Event
 */
function readMultiFile(evt) {
    multiMode = false;
    Verteilung.textEditor.reset();
    const files = evt.target.files;
    readFiles(files);
}

/**
 * liest die ausgewählten Dateien
 * @param files  die Dateien
 */
function readFiles(files) {
    try {
        Verteilung.textEditor.editor.getSession().setValue("");
        tabelle.clear();
        daten = [];
        let count = files.length;
        const maxLen = 1000000;
        let first = true;
        let reader;
        let blob;
        for (let i = 0; i < count; i++) {
            let f = files[i];
            if (f) {
                // PDF Files
                if (f.name.toLowerCase().endsWith(".pdf")) {

                    reader = new FileReader();
                    reader.onloadend = (function (theFile, clear) {
                        return function (evt) {
                            try {
                                if (evt.target.readyState === FileReader.DONE) {
                                    // Hier muss btoa verwendet werden, denn sonst wird der Inhalt der Datei nicht korrekt übertragen
                                    const content = btoa(
                                        new Uint8Array(evt.target.result)
                                            .reduce((data, byte) => data + String.fromCharCode(byte), '')
                                    );
                                    const json = executeService({"name": "extractPDFContent", "errorMessage": "PDF Datei konte nicht geparst werden:"}, [
                                        {"name": "content", "value": content}
                                    ]);
                                    if (json.success) {
                                        if (count === 1)
                                            loadText(evt.target.result, json.data, theFile.name, theFile.type, null);
                                        else
                                            loadMultiText(evt.target.result, json.data, theFile.name, theFile.type, "false", null);
                                    }
                                }
                            } catch (e) {
                                errorHandler(e);
                            }
                        };
                    })(f, first);
                    blob = f.slice(0, f.size + 1);
                    reader.readAsArrayBuffer(blob);
                }
                // ZIP Files
                if (f.name.toLowerCase().endsWith(".zip")) {
                    reader = new FileReader();
                    reader.onloadend = (function (theFile) {
                        return function (evt) {
                            try {
                                if (evt.target.readyState === FileReader.DONE) {
                                    // Hier muss btoa verwendet werden, denn sonst wird der Inhalt der Datei nicht korrekt übertragen
                                    const json = executeService({"name": "extractZIPAndExtractPDFToInternalStorage", "errorMessage": "ZIP Datei konte nicht entpackt werden:"}, [
                                        {"name": "content", "value": btoa(evt.target.result)}
                                    ]);
                                    if (json.success) {
                                        count = count + json.data - 1;
                                        const json1 = executeService({"name": "getCompleteDataFromInternalStorage"});
                                        if (json1.success) {
                                            const erg = json1.data;
                                            for (let pos in erg) {
                                                let entry = erg[pos];
                                                if (count === 1)
                                                    loadText(UTF8ArrToStr(base64DecToArr(entry.data)), entry.extractedData, entry.name, "application/zip", null);
                                                else {
                                                    // die originalen Bytes kommen decodiert, also encoden!
                                                    loadMultiText(UTF8ArrToStr(base64DecToArr(entry.data)), entry.extractedData, entry.name, entry.name.toLowerCase().endsWith(".pdf") ? "application/pdf" : "text/plain", "true",  null);
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (e) {
                                errorHandler(e);
                            }
                        };
                    })(f);
                    blob = f.slice(0, f.size + 1);
                    reader.readAsBinaryString(blob);
                }
                // Text Files
                if (f.type === "text/plain") {
                    const r = new FileReader();
                    if (files.length === 1) {
                        r.onload = (function (theFile) {
                            return function (e) {
                                loadText(e.target.result, e.target.result, theFile.name, theFile.mozFullPath, theFile.type);
                            };
                        })(f);
                    } else {
                        r.onload = (function (theFile) {
                            return function (e) {
                                loadMultiText(e.target.result, e.target.result, theFile.name,  theFile.type, "false", null);
                            };
                        })(f);
                    }
                    r.readAsText(f);
                }
            } else {
                Verteilung.textEditor.editor.getSession().setValue(Verteilung.textEditor.editor.getSession().getValue() + " Failed to load file!\n");
            }
            first = false;
        }
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * startet für alle Dokumente nochmal die Verteilung neu
 */
function doReRunAll() {
    try {
        Verteilung.textEditor.editor.getSession().setValue("");
        var tabData =  tabelle.fnGetData();
        tabelle._fnClearTable();
        for ( var i = 0; i < tabData.length; i++) {
            var name = tabData[i][1];
            REC.currentDocument.setContent(daten[name].text);
            REC.testRules(Verteilung.rulesEditor.editor.getSession().getValue());
            daten[name].log = REC.mess;
            daten[name].result = REC.results;
            daten[name].position = Verteilung.positions;
            daten[name].xml = REC.currXMLName;
            daten[name].error = REC.errors;
            var ergebnis = [];
            ergebnis["error"] = REC.errors.length > 0;
            var row = [ null,name,  REC.currXMLName.join(" : "), ergebnis, uuid(), REC.errors ];
            tabelle.fnAddData(row);
        }
    } catch (e) {
        errorHandler(e);
    }
}


/**
 * zeigt die verwendete Regel
 * @param position die Position der Regel im Text
 */
function setXMLPosition(position) {
    var session =  Verteilung.rulesEditor.editor.getSession();
    session.foldAll(1);
    var text = session.getValue();
    var rows = session.getLength();
    for (var j = 0; j< rows; j++)
         session.removeGutterDecoration(j, "ace_selectXML");
    var pos = 0;
    for ( var i = 0; i < position.length; i++)
        pos = text.indexOf("<archivTyp name=\"" + position[i] + "\"", pos);
    if (pos !== -1) {
        var pos1 = text.indexOf("</archivTyp>", pos);
        if (pos1 !== -1) {
            pos1 = pos1 + 12;
            var startRow = text.substring(0, pos).split("\n").length - 1;
            var startCol = pos - text.substring(0, pos).lastIndexOf("\n") - 1;
            var endRow = text.substring(0, pos1).split("\n").length - 1;
            var endCol = pos1 - text.substring(0, pos1).lastIndexOf("\n") - 1;
            session.unfold(startRow + 1, true);
            Verteilung.rulesEditor.editor.gotoLine(startRow + 1);
            for (var k = startRow; k <= endRow; k++)
                session.addGutterDecoration(k, "ace_selectXML");
        }
    }
}

/**
 * gibt die Ergebnisse der einzelnen Properties im entsprechenden Fenster aus
 * @param results
 * @returns {string}
 */
function printResults(results) {
    function printBlock(block ,name, indent) {
        const blanks = "                                               ";
        let ret = name +":\n";
        let maxLength = 0;
        let pos = ret.length;
        for (let key in block) {
            if (key.length > maxLength)
                maxLength = key.length;
        }
        maxLength++;
        for (let key in block) {
            if (block[key]) {
                ret = ret + blanks.substr(0, indent) + key + blanks.substr(0, maxLength - key.length) + ": " + block[key].getValue();
                if (Verteilung.positions.get(key))
                    new Position(Verteilung.POSITIONTYP.PROPS, pos + indent, pos + indent + key.length, Verteilung.positions.get(key).getCSS(), key);
                if (block[key].expected) {
                    const tmp = eval(block[key].expected);
                    if (block[key].getValue() && tmp.valueOf() === block[key].getValue().valueOf())
                        ret = ret + " [OK]";
                    else
                        ret = ret + " [FALSE] " + tmp;
                }
                ret = ret + "\n"  ;
                pos = ret.length;
            }
        }
        return ret;
    }

    let ret = "";
    ret = ret + printBlock(results.search, "Suche", 2);
    ret = ret + printBlock(results.tag, "Tags", 2);
    ret = ret + printBlock(results.category, "Kategorien", 2);
    return ret;
}


/**
 * stellt die Funktionalität für den Zurück Button zur Verfügung
 */
function doBack() {
    try {
        multiMode = true;
        showMulti = false;
        Verteilung.textEditor.editor.getSession().setValue("");
        Verteilung.propsEditor.editor.getSession().setValue("");
        Verteilung.rulesEditor.editor.getSession().foldAll(1);
        manageControls();
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * prüft die Regeln
 * @param xmlContent die Regelm als String
 * @returns {boolean} true wenn die Regeln syntaktisch korrekt sind.
 */
function validateXML(xmlContent) {
    let schemaContent;
    let validateErrors;
    if (rulesSchemaId) {
        const json = executeService({
            "name": "getDocumentContent",
            "errorMessage": "Schema konnten nicht gelesen werden:"
        }, [
            {"name": "documentId", "value": rulesSchemaId}
        ]);
        if (json.success) {
            schemaContent = decodeBase64(json.data);
        }

        if (schemaContent) {
            validateErrors = xmllint.validateXML({xml: xmlContent, schema: schemaContent}).errors;
            if (validateErrors) {
                for (let i = 0; i < validateErrors.length; i++) {
                    const err = validateErrors[i];
                    if (err.startsWith("file_0.xml")) {
                        const line = err.split(":")[1];
                        new Position(Verteilung.POSITIONTYP.RULES, line, 0, line, 1, "ace_error", "fullLine");
                    }
                    Logger.log(Level.ERROR, validateErrors[i]);
                }
            }
        }
    }
    return !validateErrors;
}

/**
 * Funktionalität für den Run Button
 */
function work() {
    try {
        var json;
        var schemaContent;
        var validate = true;
        // aktuelles Verteilungsskript vom Server holen
        if (scriptID) {
            // ScriptID ist vorhanden, wir versuchen das Skript vom Alfresco Server zu laden
            json = executeService({
                "name": "getDocumentContent",
                "errorMessage": "Skript konnte nicht gelesen werden:"
            }, [
                {"name": "documentId", "value": scriptID}
            ]);
            if (json.success) {
                // Logger retten
                const logger = Logger;
                eval("//# sourceURL=recognition.js\n\n" + decodeBase64(json.data));
                Logger = logger;
            }
        }
        var selectMode = false;
        if (multiMode)
            doReRunAll();
        else {
            var range = Verteilung.rulesEditor.editor.getSelectionRange();
            var sel = Verteilung.rulesEditor.editor.getSession().getTextRange(range);
            if (sel.length > 0) {
                if (!sel.startsWith("<")) {
                    var start = Verteilung.rulesEditor.editor.find('<', {
                        backwards: true,
                        wrap: false,
                        caseSensitive: false,
                        wholeWord: false,
                        start: range,
                        regExp: false
                    });
                    if (start)
                        range.setStart(start.start);
                }
                if (!sel.endsWith("/>")) {
                    var end = Verteilung.rulesEditor.editor.find('>', {
                        backwards: false,
                        wrap: false,
                        caseSensitive: false,
                        wholeWord: false,
                        start: new Verteilung.Range(range.end.row, range.end.column, range.end.row, range.end.column),
                        regExp: false
                    });
                    if (end)
                        range.setEnd(end.end);
                }
                sel = Verteilung.rulesEditor.editor.getSession().getTextRange(range);
                if (!sel.endsWith("/>")) {
                    var tmp = sel.substring(1, sel.indexOf(" "));
                    tmp = "</" + tmp + ">";
                    end = Verteilung.rulesEditor.editor.find(tmp, {
                        backwards: false,
                        wrap: false,
                        caseSensitive: false,
                        wholeWord: false,
                        start: new Verteilung.Range(range.end.row, range.end.column, range.end.row, range.end.column),
                        regExp: false
                    });
                    range.setEnd(end.end);
                }
                Verteilung.rulesEditor.editor.selection.setSelectionRange(range);
                sel = Verteilung.rulesEditor.editor.getSession().getTextRange(range);
                if (!sel.startsWith("<tags") && !sel.startsWith("<category") && !sel.startsWith("<archivPosition")) {
                    selectMode = true;
                    if (!sel.startsWith("<searchItem ")) {
                        start = Verteilung.rulesEditor.editor.find('<searchItem', {
                            backwards: true,
                            wrap: false,
                            caseSensitive: false,
                            wholeWord: false,
                            start: range,
                            regExp: false
                        });
                        if (start)
                            range.setStart(start.start);
                        end = Verteilung.rulesEditor.editor.find('</searchItem>', {
                            backwards: false,
                            wrap: false,
                            caseSensitive: false,
                            wholeWord: false,
                            start: new Verteilung.Range(range.end.row, range.end.column, range.end.row, range.end.column),
                            regExp: false
                        });
                        if (end)
                            range.setEnd(end.end);
                        Verteilung.rulesEditor.editor.selection.setSelectionRange(range);
                        sel = Verteilung.rulesEditor.editor.getSession().getTextRange(range);
                    }
                    if (!sel.startsWith("<archivTyp "))
                        sel = "<archivTyp name='' searchString=''>" + sel;
                    if (!sel.endsWith("</archivTyp>"))
                        sel = sel + "</archivTyp>";
                    if (!sel.startsWith("<documentTypes "))
                        sel = "<documentTypes>" + sel;
                    if (!sel.endsWith("</documentTypes>"))
                        sel = sel + "</documentTypes>";
                } else
                    sel = Verteilung.rulesEditor.editor.getSession().getValue();
            } else
                sel = Verteilung.rulesEditor.editor.getSession().getValue();
            REC.init();
            REC.currentDocument.properties.content.write(new Content(Verteilung.textEditor.editor.getSession().getValue()));
            REC.currentDocument.name = Verteilung.textEditor.file ? Verteilung.textEditor.file : "DocumentOhneNamen";
            $.each(Verteilung.textEditor.editor.getSession().getMarkers(false), function(element, index) {Verteilung.textEditor.editor.getSession().removeMarker(element)});
            $.each(Verteilung.rulesEditor.editor.getSession().getMarkers(false), function(element, index) {Verteilung.rulesEditor.editor.getSession().removeMarker(element)});
            $.each(Verteilung.propsEditor.editor.getSession().getMarkers(false), function(element, index) {Verteilung.propsEditor.editor.getSession().removeMarker(element)});
            Verteilung.positions.clear();

            if (validateXML(sel)) {
                REC.testRules(sel);
                if (!selectMode) {
                    setXMLPosition(REC.currXMLName);
                    alertify.success("Erkennung beendet");
                }
            } else
                alertify.alert("Fehler", "Regeln sind syntaktisch nicht korrekt!");
            Verteilung.propsEditor.editor.getSession().setValue(printResults(REC.results));
            Verteilung.positions.setMarkers();
            document.getElementById('inTxt').style.display = 'block';
            document.getElementById('dtable').style.display = 'none';
        }
    } catch (e) {
        errorHandler(e);
    }
}


/**
 * aktualisiert die geänderten Regeln auf dem Server
 * @returns {boolean}  liefert true zurück, wenn alles geklappt hat
 */
function sendRules() {
    try {
        let erg = false;
        if (currentRules.endsWith("doc.xml")) {
            const xmlContent = Verteilung.rulesEditor.editor.getSession().getValue();
             if (!validateXML(xmlContent)) {
                 $.each(Verteilung.rulesEditor.editor.getSession().getMarkers(false), function(element, index) {Verteilung.rulesEditor.editor.getSession().removeMarker(element)});
                 Verteilung.positions.setMarkers();
                 alertify.alert("Fehler", "Regeln sind syntaktisch nicht korrekt!");
             } else {

                 const json = executeService({
                     "name": "updateDocument",
                     "errorMessage": "Regeln konnten nicht übertragen werden:"
                 }, [
                     {"name": "documentId", "value": rulesID},
                     {
                         "name": "content",
                         "value": vkbeautify.xml(xmlContent),
                         "type": "byte"
                     },
                     {"name": "mimeType", "value": "text/xml"},
                     {"name": "extraProperties", "value": {}},
                     {"name": "versionState", "value": "minor"},
                     {"name": "versionComment", "value": ""}
                 ]);
                 if (json.success) {
                     Logger.log(Level.INFO, "Regeln erfolgreich zum Server übertragen!");
                     rulesID = json.data.objectId;
                     erg = true;
                 }
             }
            return erg;
        }
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * liest die Regeln entweder vom Server oder lokal von der Platte
 * @param rulesId   die DokumentenId der Regeln auf dem Server
 * @param loadLocal legt fest, ob lokal oder vom Server gelesen werden soll
 */
function getRules(rulesId, loadLocal) {
    try {
        let ret;
        if (loadLocal) {
            Verteilung.rulesEditor.editor.getSession().setValue(openFile("./rules/doc.xml"));
            Verteilung.rulesEditor.editor.getSession().foldAll(1);
            Logger.log(Level.INFO, "Regeln erfolgreich lokal gelesen!");
        } else {
            const json = executeService({"name": "getDocumentContent", "errorMessage": "Regeln konnten nicht gelesen werden:"}, [
                {"name": "documentId", "value": rulesID}
            ]);
            if (json.success) {
                Verteilung.rulesEditor.editor.getSession().setValue(decodeBase64(json.data));
                Verteilung.rulesEditor.editor.getSession().foldAll(1);
                Logger.log(Level.INFO, "Regeln erfolgreich vom Server übertragen!");
            } else
                alertify.alert("Fehler", "Fehler bei der Übertragung: " + json.data);
        }
        currentRules = "doc.xml";
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * Öffnet die Regeln
 */
function openRules() {
    try {
        if (rulesID && typeof rulesID === "string") {
            const id = rulesID.substring(rulesID.lastIndexOf('/') + 1);
            getRules(id, false);
            document.getElementById('headerCenter').textContent = "Regeln (Server: doc.xml)";
        } else {
            $.get('./rules/doc.xml', function (msg) {
                Verteilung.rulesEditor.editor.getSession().setValue(new XMLSerializer().serializeToString(msg));
                Verteilung.rulesEditor.editor.getSession().foldAll(1);
                currentRules = "doc.xml";
            });
            document.getElementById('headerCenter').textContent = "Regeln (doc.xml)";
            //	window.parent.frames.rules.Verteilung.rulesEditor.editor.getSession().setValue("Regeln konnten nicht geladen werden!");
        }
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * formatiert die Regeln
 */
function format() {
    try {
        Verteilung.rulesEditor.editor.getSession().setValue(vkbeautify.xml(Verteilung.rulesEditor.editor.getSession().getValue()));
        // window.parent.frames.rules.Verteilung.rulesEditor.editor.getSession().foldAll(1);
        if (REC.currXMLName) {
            setXMLPosition(REC.currXMLName);
            Verteilung.positions.setMarkers();
        }
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * formatiert das Script
 */
function formatScript() {
    try {
        Verteilung.textEditor.editor.getSession().setValue(js_beautify(Verteilung.textEditor.editor.getSession().getValue()));
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * öffnet eine lokale Datei
 * @param file  der Name der Datei
 * @returns den Inhalt
 */
function openFile(file) {
    try {
        const json = executeService({"name": "openFile", "errorMessage": "Datei konnte nicht geöffnet werden:"}, [
            {"name": "filePath", "value": convertPath(file)}
        ]);
        if (json.success) {
            Logger.log(Level.INFO, "Datei " + name + " erfolgreich geöffnet!");
            return UTF8ArrToStr(base64DecToArr(json.data));
        }
        else
            return "";
    } catch (e) {
        errorHandler(e);
    }
}


/**
 * Eventhandler der die Verarbeitung von fallen gelassen Dateien auf den Regelbereich zuständig ist
 * @param evt  das Event
 */
function handleRulesSelect(evt) {
    evt.stopPropagation();
    evt.preventDefault();
    let files = evt.dataTransfer.files;
    for ( let i = 0; i < files.length; i++) {
        const f = files[i];
        if (f) {
            const r = new FileReader();
            r.onload = function(e) {
                const contents = e.target.result;
                Verteilung.rulesEditor.editor.getSession().setValue(contents);
                Verteilung.rulesEditor.editor.getSession().foldAll(1);
            };
            r.readAsText(f);
        } else {
            alertify.alert("Fehler", "Failed to load file!");
        }
    }
}


/**
 * lädt das auf dem Server gespeicherte Verteilungsscript
 */
function getScript() {
    const fetchScript = function () {
        const json = executeService({"name": "getDocumentContent", "errorMessage": "Skript konnte nicht gelesen werden:"}, [
            {"name": "documentId", "value": scriptID}
        ]);
        if (json.success) {
            Verteilung.textEditor.editor.getSession().setValue(decodeBase64(json.data));
            Logger.log(Level.INFO, "Script erfolgreich heruntergeladen!");
        }
    };
    try {
        if (!Verteilung.textEditor.editor.getSession().getUndoManager().isClean()) {
            const $dialog = $('<div></div>').html('Skript wurde geändert!<br>Neu laden?').dialog({
                autoOpen: true,
                title: "Skript laden",
                modal: true,
                height: 150,
                width: 200,
                buttons: {
                    "Ok": function () {
                        try {
                            fetchScript();
                            $(this).dialog("destroy");
                        } catch (e) {
                            errorHandler(e);
                        }
                    },
                    "Abbrechen": function () {
                        $(this).dialog("destroy");
                    }
                }
            });
        } else {
            fetchScript();
        }
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * öffnet den Editor für das Verteilungsskript
 */
function openScript() {
    try {
        panelSizeReminder = verteilungLayout.state.west.size;
        // 84% sind das Maximum, danach ist das Fenster mit den Regel ganz verschwunden und beim Schliessen
        // des Scriptes die Darstellung der Regeln kaputt
        verteilungLayout.sizePane("west", "84%");
        Verteilung.oldContent = Verteilung.textEditor.editor.getSession().getValue();
        let content, json, script;
        let read = false;
        if (Verteilung.modifiedScript && Verteilung.modifiedScript.length > 0) {
            content = Verteilung.modifiedScript;
        } else {
            if (scriptID) {
                // ScriptID ist vorhanden, wir versuchen das Skript vom Alfresco Server zu laden
                json = executeService({"name": "getDocumentContent", "errorMessage": "Skript konnte nicht gelesen werden:"}, [
                    {"name": "documentId", "value": scriptID}
                ]);
                if (json.success) {
                    content = decodeBase64(json.data);
                    read = true;
                    Logger.log(Level.INFO, "Script erfolgreich vom Server geladen!");
                }
            }
            else {
                // wir laufen im Servlet und versuchen das Skript vom Server zu bekommen weil kein Alfresco Server da ist.
                script = $.ajax({
                    url: "./js/recognition.js",
                    async: false
                }).responseText;
            }
            if (script && script.length > 0) {
                content = script;
                read = true;
                Logger.log(Level.INFO, "Script erfolgreich gelesen!");
            }
        }
        if (read) {
            workDocument = "recognition.js";
            const tmp = REC.mess;
            eval("//# sourceURL=recognition.js\n\n" + content);
            REC.mess = tmp;
            $.each(Verteilung.textEditor.editor.getSession().getMarkers(false), function(element, index) {Verteilung.textEditor.editor.getSession().removeMarker(element)});
            $.each(Verteilung.rulesEditor.editor.getSession().getMarkers(false), function(element, index) {Verteilung.rulesEditor.editor.getSession().removeMarker(element)});
            $.each(Verteilung.propsEditor.editor.getSession().getMarkers(false), function(element, index) {Verteilung.propsEditor.editor.getSession().removeMarker(element)});
            Verteilung.textEditor.editor.getSession().setMode("ace/mode/javascript");
            Verteilung.textEditor.editor.getSession().setValue(content);
            Verteilung.textEditor.editor.setShowInvisibles(false);
            Verteilung.textEditor.editor.getSession().getUndoManager().markClean();
            Verteilung.textEditor.scriptMode = true;
            manageControls();
        }
    } catch (e) {
        errorHandler(e);
        // if (layoutState)
        //     verteilungLayout.sizePane("west", layoutState.west.size);
    }
}


/**
 * lädt ein geändertes Verteilungsscript in den Kontext der Anwendung, damit die Änderungen wirksam werden
 */
function activateScriptToContext() {
    try {
        Verteilung.modifiedScript = Verteilung.textEditor.editor.getSession().getValue();
        // Logger retten
        const logger = Logger;
        eval("//# sourceURL=recognition.js\n\n" + Verteilung.modifiedScript);
        Logger = logger;
        const mess = "Die gändeterten Skriptanweisungen sind jetzt wirksam!";
        alertify.success(mess);
        Logger.log(Level.INFO, mess);
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * sendet das Script zum Server
 * @returns {boolean}  true, wenn alles geklappt hat
 */
function sendScript() {
    let erg = false;
    try {
        if (workDocument.endsWith("recognition.js")) {
            const script = Verteilung.textEditor.editor.getSession();
            // Logger retten
            const logger = Logger;
            eval("//# sourceURL=recognition.js\n\n" + script);
            Logger = logger;

            const json = executeService({"name": "updateDocument", "errorMessage": "Skript konnte nicht zum Server gesendet werden:"}, [
                {"name": "documentId", "value": scriptID},
                {"name": "content", "value": script, "type": "byte"},
                {"name": "mimeType", "value": "application/javascript"},
                {"name": "extraProperties", "value": {}},
                {"name": "versionState", "value": "minor"},
                {"name": "versionComment", "value": ""}
            ]);
            if (json.success) {
                Logger.log(Level.INFO, "Script erfolgreich zum Server gesendet!");
                scriptID = json.data.objectId;
                erg = true;
            }
        }
        return erg;
    } catch (e) {
        errorHandler(e);
        return false;
    }
}

/**
 * sendet ein Dokument zur Inbox
 */
function sendToInbox() {
    try {
        const json = executeService({"name": "createDocument", "errorMessage": "Dokument konnte nicht auf den Server geladen werden:", "successmessage": "Dokument " + name + " wurde erfolgreich in die Inbox verschoben!"}, [
            {"name": "documentId", "value": inboxFolderId},
            {"name": "fileName", "value": Verteilung.textEditor.file},
            {"name": "content", "value": Verteilung.textEditor.content.original, "type": "byte"},
            // TODO Richtigen Mimetype ermitteln
            {"name": "mimeType", "value": Verteilung.textEditor.typ},
            {"name": "extraProperties", "value": {}},
            {"name": "versionState", "value": "none"}
        ]);
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * schliesst den Scripteditor
 */
function closeScript() {
    try {
        verteilungLayout.sizePane("west", panelSizeReminder);
        Verteilung.textEditor.editor.getSession().setMode("ace/mode/text");
        if (Verteilung.oldContent && Verteilung.oldContent.length > 0)
            Verteilung.textEditor.editor.getSession().setValue(Verteilung.oldContent);
        else
            Verteilung.textEditor.editor.getSession().setValue("");
        Verteilung.textEditor.editor.setShowInvisibles(true);
        Verteilung.textEditor.scriptMode = false;
        manageControls();
    } catch (e) {
        errorHandler(e);
    }
}

var Verteilung = {
    rulesEditor:  {
        editor: null,
        fontsize: 10,
    },
    textEditor:  {
        editor: null,
        scriptMode: false,
        file: false,
        typ: false,
        content: {
            original: false,
            asText: false
        },
        fontsize: 10,
        reset: function(){
            this.scriptMode = false;
            this.file = false;
            this.typ = false;
            this.content.original = false;
            this.content.asText = false;
        }
    },
    propsEditor:  {
        editor: null,
        fontsize: 10,
    },
    outputEditor: {
        editor: null,
        fontsize: 10,
    },
    oldContent:  null,
    modifiedScript: null,
    positions: new PositionContainer(),
    POSITIONTYP: {
        TEXT : {value: 0, name: "Text", editor: "Verteilung.textEditor.editor"},
        RULES: {value: 1, name: "Rules", editor: "Verteilung.rulesEditor.editor"},
        PROPS : {value: 2, name: "Pros", editor: "Verteilung.propsEditor.editor"}
    }
};
if (typeof ace !== "undefined")
        Verteilung.Range = ace.require("ace/range").Range;


