/**
 * Created by m500288 on 11.09.14.
 */

/**
 * Prüft, ob ein Alfresco Server antwortet
 * @param url         URL des Servers
 * @returns {boolean} true, wenn der Server verfügbar ist
 */
function checkServerStatus(url) {

    var obj = executeService({"name": "isURLAvailable", "ignoreError": true}, [{
        "name": "server",
        "value": url
    }, {"name": "timeout", "value": "5000"}]);
    return obj.data.toString() === "true";
}

/**
 * führt einen Service aus
 * @param service           JSON Object mit dem Service
 *                          name            Name des Services
 *                          callback        Funktion, die ausgeführt werden soll, wenn der Aufruf asynchron erfolgen soll
 *                          errorMessage    Fehlermeldung
 *                          successMessage  Erfolgsmeldung
 *                          ignoreError     Flag, ob ein Fehler ignoriert werden soll
 *                          direct          Flag, dass das Ergebnis direkt zurückgegegeben werden soll. In diesen Fällen kommt kein JSON zurück
 *                          url             Url für den Aufruf Wird eigentlich nur für Tests gebraucht
 * @param params            die Parameter als JSON Objekt
 *                          name:  der Name des Parameters
 *                          value: der Inhalt des Paramaters
 *                          type: der Typ des Parameters
 * @return das Ergebnis als JSON Objekt
 *                          data            das Ergebnis der Anbfrage
 *                          success         Boolean der den Erfolg der Services speichert
 *                          error           eventuelle Fehler
 *                          duration        die Zeit, die der Service gebraucht hat
 * Diese Methode kann nicht aufgerufen werdeb bevor die abhängigen Bibliotheken geladen werden weil diese auch von der Methode
 * benutzt werden. Dies ist unter anderem jQuery.
 */
function executeService(service, params) {
    var json = null;
    var index;
    var errorMessage;
    var successMessage;
    var done;
    var url = window.location.pathname === "/context.html" ? "http://localhost:8080/Archiv/" : window.location.pathname.substring(0, window.location.pathname.indexOf("/", 2)) + "/";
    var times = [];
    try {
        if (service.errorMessage)
            errorMessage = service.errorMessage;
        if (service.successMessage)
            successMessage = service.successMessage;
        Logger.log(Level.DEBUG, "Execute: " + service.name);
        times.push(new Date().getTime());
        var asynchron = true;
        if (!service.callback) {
            asynchron = false;
            done = function (data) {
                json = data;
            }
        } else {
            done = service.callback;
            if (window.location.pathname === "/context.html" )
                asynchron = false; // im Test immer auf synchron umschalten!
        }
        if (service.url) {
            url = service.url;
            if (!url.endsWith("/"))
                url = url + "/";
        }
        var dataString = {};
        if (params) {
            for (index = 0; index < params.length; ++index) {
                // falls Bytecode übertragen werden soll, dann Umwandlung damit es nicht zu Konvertierungsproblemen kommt
                if (params[index].type && params[index].type === "byte")
                    // Hier nicht btoa verwenden, weil es sonst Probleme mit Umlauten gibt
                    params[index].value = base64EncArr(strToUTF8Arr((params[index].value)));
                dataString[params[index].name] = params[index].value;
            }
        }
        $.ajax({
            type: "POST",
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(dataString),
            datatype: "json",
            cache: false,
            headers: {"cache-control": "no-cache"},
            async: asynchron,
            url: url + service.name,
            error: function (xhr, status, error) {
                try {
                    var txt;
                    var statusErrorMap = {
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
                    if (!service.ignoreError)
                        message("Fehler", "Status: " + txt + "<br>Response: " + xhr.responseText);
                    json = {error: txt, success: false, data: null};
                    
                } catch (e) {
                    var str = "FEHLER:\n";
                    str = str + e.toString() + "\n";
                    for (var prop in e)
                        str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                    if (!service.ignoreError)
                        message("Fehler", str + "<br>" + xhr.responseText);
                    else {
                        json = {error: str, success: false, data: null};
                    }
                }
            },
            success: function (data) {
                try {
                    if (service.direct) {
                        // es kommt keine JSON Struktur zurück
                        json = data;
                    } else if (!data.success) {
                        if (errorMessage)
                            errorString = errorMessage + "<br>";
                        else {
                            if (data.data)
                                errorString = data.data;
                            if (data.error)
                                errorString = data.error.message;
                        }
                        // gibt es eine Fehlermeldung aus dem Service?
                        if (data.error && !service.ignoreError) {
                            errorString = errorString + "<br>" + data.error.message;
                            Logger.log(Level.ERROR, data.error.message);
                        }
                        if (!service.ignoreError && data.data) {
                            Logger.log(Level.ERROR, data.data);
                        }
                        json = data;
                    } else {
                        times.push(new Date().getTime());
                        data.duration = times[1] - times[0];
                        Logger.log(Level.INFO, "Execution of Service: " + service.name + " duration " + (times[1] - times[0]) + " ms");
                        if (successMessage) {
                            Logger.log(Level.INFO, successMessage);
                        }
                        done(data);
                    }
                } catch (e) {
                    errorHandler(e);
                }
            }
        });
        return json;
    } catch (e) {
        var p = "Service: " + service.name + "<br>";
        if (params) {
            for (index = 0; index < params.length; ++index) {
                p = p + "Parameter: " + params[index].name;
                if (params[index].value && typeof params[index].value === "string")
                    p = p + " : " + params[index].value.substr(0, 40) + "<br>";
                else
                    p = p + " : Parameter Value fehlt!<br>";
            }
        }
        if (errorMessage)
            p = errorMessage + "<br>" + e.toString() + "<br>" + p;
        else
            p = errorMessage + "<br>" + e.toString();
        if (!service.ignoreError)
            errorHandler(e, p);
        return {error: e, success: false, data: null};
    }
}


/**
 * liefert die Einstellungen
 * wenn noch keine Einstellungen gesetzt sind, dann sucht die Funktion einen passenden URL-Parameter
 * und trägt diesen dann ein. Ist dieser auch nicht vorhanden, dann wird <null> zurück geliefert.
 * @param key    Schlüssel der Einstellung
 * @returns {*}  Den Wert der Einstellung
 */
function getSettings(key) {

    if (!settings || settings.settings.filter(function (o) {
            return o.key.indexOf(key) >= 0;
        }).length === 0) {
        var urlPar = getUrlParam(key);
        if (!urlPar)
            return null;
        else {
            if (!settings)
                settings = {settings: []};
            settings.settings.push({"key": key, "value": urlPar});
        }
    }
    return settings.settings.filter(function (o) {
        return o.key.indexOf(key) >= 0;
    })[0].value;
}

/**
 * gibt einen aktuellen Timestamp zurück "m/d/yy h:MM:ss TT"
 * @param withDate mit Datum
 * @type {Date}
 */
function timeStamp(withDate) {
    var returnString = "";
    var now = new Date();
    var time = [now.getHours(), now.getMinutes(), now.getSeconds()];
    for (var i = 1; i < 3; i++) {
        if (time[i] < 10) {
            time[i] = "0" + time[i];
        }
    }
    if (withDate) {
        var date = [now.getMonth() + 1, now.getDate(), now.getFullYear()];
        returnString = returnString + date.join(".") + " ";
    }
    returnString += time.join(":");
    return returnString;
}


/**
 * sucht eine Key / Value Kombination aus einer JSON Struktur
 * @param obj           das zu durchsuchende JSON Objekt
 * @param key           der Key
 * @param val           der Wert
 * @return {Array}      das Ergebnis
 */
function searchJson(obj, key, val) {
    var objects = [];
    for (var i in obj) {
        if (!obj.hasOwnProperty(i)) continue;
        if (typeof obj[i] === 'object') {
            objects = objects.concat(searchJson(obj[i], key, val));
        } else
//if key matches and value matches or if key matches and value is not passed (eliminating the case where key matches but passed value does not)
        if (i === key && obj[i] === val || i === key && val === '') { //
            objects.push(obj);
        } else if (obj[i] === val && key === '') {
//only add if the object is not already in the array
            if (objects.lastIndexOf(obj) === -1) {
                objects.push(obj);
            }
        }
    }
    return objects;
}

/**
 * ändert das CSS für eine bestimmte Klasse
 * @param className   der Name der Class
 * @param classValue  der neue Wert
 */
function changeCss(className, classValue) {
    var cssMainContainer = $('#css-modifier-container');
    if (cssMainContainer.length === 0) {
        cssMainContainer = $('<div id="css-modifier-container"></div>');
        cssMainContainer.hide();
        cssMainContainer.appendTo($('body'));
    }
    classContainer = cssMainContainer.find('div[data-class="' + className + '"]');
    if (classContainer.length === 0) {
        classContainer = $('<div data-class="' + className + '"></div>');
        classContainer.appendTo(cssMainContainer);
    }
    classContainer.html('<style>' + className + ' {' + classValue + '}</style>');
}

/**
 * prüft, ob ein String mit einem Vergleichsstring endet
 * @param str               der Vergleichsstring
 * @returns {boolean}
 */
String.prototype.endsWith = function (str) {
    return (this.match(str + "$") == str);
};

/**
 * prüft, ob ein String mit einem Vergleichsstring beginnt
 * @param str               der Vergleichsstring
 * @returns {boolean}
 */
String.prototype.startsWith = function (str) {
    return (this.match("^" + str) == str);
};

/**
 * parst einen Alfresco Datums String
 * @param dateString   der Datumsstring
 */
function parseDate(dateString) {
    try {
        var months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
        var parts = dateString.split(" ");
        var month = parts[1];
        var day = parts [2];
        var year = parts[5];
        var timeString = parts[3];
        parts = timeString.split(":");
        var hours = parts[0];
        var minutes = parts[1];
        var seconds = parts[2];
        return new Date(year, months.indexOf(month), day, hours, minutes, seconds, 0);
    } catch (e) {
        return null;
    }
}

/**
 * liefert das aktuelle Tagesdatum
 * @param formatString der String zum formatieren
 * @return {number}
 */
function getCurrentDate(formatString) {
    {
        return Formatter.dateFormat(new Date(), formatString)
    }
}

/**
 * liefert einen Url-Paramter
 * @param name          der Name des Parameters
 * @returns {String}
 */
function getUrlParam(name) {
    name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
    var regexS = "[\\?&]" + name + "=([^&#]*)";
    var regex = new RegExp(regexS);
    var results = regex.exec(this.location.href);
    if (!results)
        return null;
    else
        return decodeURIComponent(results[1]);
}


/**
 * Prüft, ob ein URL Parameter vorhanden ist
 * @returns {boolean}
 */
function hasUrlParam() {
    return this.location.href.search(/\?/) !== -1;
}

/**
 * erstellt einen vollständigen Pfad zum übegebenen Dateipfad
 * @param filePath     der übergebene Dateipfad
 * @returns {String}   den kompletten Pfad zur Datei auf dem Serveer
 */
function createPathToFile(filePath) {
    var file = document.URL;
    var parts = file.split("/").reverse();
    parts.splice(0, 1);
    if (!filePath.startsWith("/"))
        filePath = "/" + filePath;
    return parts.reverse().join("/") + filePath;
}

/**
 * konvertiert den Pfad in einen absoluten Pfad
 * @param name
 * @returns {string}
 */
function convertPath(name) {
    return "file://" + window.location.pathname.substring(0, window.location.pathname.lastIndexOf("/") + 1) + name;
}


/**
 * Globale Fehlerroutine
 * @param e             der auslösende Fehler
 * @param description   optionale Fehlermeldung
 */
function errorHandler(e, description) {
    var str;
    if (description)
        str = description + "<br>FEHLER:<br>";
    else
        str = "FEHLER:<br>";
    str = str + e.toString() + "<br>";
    for (var prop in e)
        str = str + "property: " + prop + " value: [" + e[prop] + "]<br>";
    str = str + "Stacktrace: <br>" + e.stack;
    message("Fehler", str);
}

/**
 * zeigt eine Meldung
 * @param title      Titel des Fensters
 * @param str        Meldungstext
 * @param autoClose  Wert für den Timeout beim automatischen Schliessen der Message
 * @param height     Höhe des Gensters
 * @param width      Breite des Fensters
 * TODO Message für einfachen Dialog mit Ja/Nein oder Ok/Cancel aufbohren
 */
function message(title, str, autoClose, height, width) {
    if (!height)
        height = 200;
    if (!width)
        width = 800;
    var dialogSettings = {
        autoOpen: false,
        title: title,
        modal: true,
        height: height,
        width: width
    };
    var div = $("<div></div>");
    if (autoClose) {
        dialogSettings.open = function (event, ui) {
            setTimeout("$('#messageBox').dialog('close')", autoClose);
        }
    } else {
        dialogSettings.buttons = {
            "Ok": function () {
                $(this).dialog("destroy");
                div.remove();
            }
        }
    }

    var $dialog = div.html(str).dialog(dialogSettings).css({
        height: height + "px",
        width: width + "px",
        overflow: "auto"
    });
    $dialog.dialog('open');
}


/**
 * konvertiert Bytes zu einem String
 * @param hexx          die zu konvertierenden Bytes
 * @return {string}     der Ergebnis String
 */
function hex2String(hexx) {
    var hex = hexx.toString(); //force conversion
    var str = '';
    for (var i = 0; i < hex.length; i += 2)
        str += String.fromCharCode(parseInt(hex.substr(i, 2), 16));
    return str;
}

/**
 * konvertiert einen String zu einem Bytearray
 * @param str        der zu konvertierende String
 * @return {Array}   der String als Bytearray
 */
function stringToBytes(str) {
    var ch, st, re = [], j = 0;
    for (var i = 0; i < str.length; i++) {
        ch = str.charCodeAt(i);
        if (ch < 127) {
            re[j++] = ch & 0xFF;
        } else {
            st = [];
            // clear stack
            do {
                st.push(ch & 0xFF);
                // push byte to stack
                ch = ch >> 8;
                // shift value down by 1 byte
            } while (ch);
            // add stack contents to result
            // done because chars have "wrong" endianness
            st = st.reverse();
            for (var k = 0; k < st.length; ++k)
                re[j++] = st[k];
        }
    }
    // return an array of bytes
    return re;
}

/**
 * generiert eine eindeutige Id
 * @return  {string}
 */
function uuid() {
    var chars = '0123456789abcdef'.split('');
    var uuid = [], rnd = Math.random, r;
    uuid[8] = uuid[13] = uuid[18] = uuid[23] = '-';
    uuid[14] = '4';
    // version 4
    for (var i = 0; i < 36; i++) {
        if (!uuid[i]) {
            r = 0 | rnd() * 16;
            uuid[i] = chars[(i === 19) ? (r & 0x3) | 0x8 : r & 0xf];
        }
    }
    return uuid.join('');
}


/**
 * gibt die Meldungen im entsprechenden Fenster aus
 * @param reverse   die Reihenfolge wird umgedreht
 * @param level     der Mindestlevel
 */
function fillMessageBox(reverse, level) {
    if (Verteilung.outputEditor) {
        if (!level)
            level = Logger.getLevel();
        Verteilung.outputEditor.getSession().setValue(Logger.getMessages(reverse, level));
    }
}

/**
 * erstellt ein neues Dokument
 * @param input             die neu einzutragenden Daten
 * @param file              das File Objekt
 */
function createDocument(input, file) {
    var erg = {"success": false};
    reader = new FileReader();
    reader.onloadend = (function (theFile) {
        return function (evt) {
            try {
                if (evt.target.readyState === FileReader.DONE) {
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
                            // Tabelle updaten
                            alfrescoTabelle.row.add(json.data).draw();
                        }
                    };
                    erg = executeService({
                        "name": "createDocument",
                        "callback": done,
                        "errorMessage": "Dokument konnte nicht erstellt werden!",
                        "ignoreError": false
                    }, [
                        {"name": "documentId", "value": tree.jstree("get_selected")[0]},
                        {"name": "fileName", "value": theFile.name},
                        // Hier muss btoa verwendet werden, denn sonst wird der Inhalt der Datei nicht korrekt übertragen
                        {"name": "content", "value": btoa(evt.target.result)},
                        {"name": "extraProperties", "value": extraProperties},
                        // TODO Richtigen Mimetype ermitteln
                        {"name": "mimeType", "value": "application/pdf"},
                        {"name": "versionState", "value": "major"}
                    ]);
                }
            } catch (e) {
                errorHandler(e);
            }
        };
    })(file);
    blob = file.slice(0, file.size + 1);
    reader.readAsBinaryString(blob);
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
                var row = alfrescoTabelle.row('#' + origData.objectID);
                if (row && row.length)
                    row.remove().draw(false);
                row = alfrescoSearchTabelle.row('#' + origData.objectID);
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
                if (lastElement && lastElement.get(0).id === newData.parentId) {
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
                if (lastElement && lastElement.get(0).id === origData.parentId) {
                    var row = alfrescoFolderTabelle.row('#' + origData.objectID);
                    if (row && row.length) {
                        row.remove().draw();
                    }
                }
                // der aktuelle Ordner ist der zu löschende
                if (lastElement && lastElement.get(0).id === origData.objectID) {
                    tree.select_node(origData.parentId);
                } else {
                    // BreadCrumb aktualisieren
                    if (lastElement)
                        fillBreadCrumb(lastElement.data().data);
                }
            }

        };
        var erg = executeService({"name": "deleteFolder", "callback": done, "errorMessage": "Ordner konnte nicht gelöscht werden!"}, [
            {"name": "documentId", "value": origData.objectID}
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
                if (lastElement && lastElement.get(0).id === newData.parentId) {
                    var row = alfrescoFolderTabelle.row('#' + newData.objectID);
                    if (row && row.length) {
                        row.data(newData).invalidate();
                    }
                }
                // BreadCrumb aktualisieren
                if (lastElement && lastElement.get(0).id === id) {
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






