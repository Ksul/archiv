/*
 * jQuery Superfish Menu Plugin
 * Copyright (c) 2013 Joel Birch
 *
 * Dual licensed under the MIT and GPL licenses:
 *	http://www.opensource.org/licenses/mit-license.php
 *	http://www.gnu.org/licenses/gpl.html
 * Achtung Code geändert!!
 */

(function ($, w) {
    "use strict";

    var methods = (function () {
        // private properties and methods go here
        var c = {
                bcClass: 'sf-breadcrumb',
                menuClass: 'sf-js-enabled',
                anchorClass: 'sf-with-ul',
                menuArrowClass: 'sf-arrows'
            },
            rootMenu = null,
            setRootMenu = function(menu){
                rootMenu = menu;
            },
            getRootMenu = function(){
                return rootMenu;
            },
            recursiveKeySearch = function (key, data) {
                // not shown - perhaps validate key as non-zero length string

                // Handle null edge case.
                if (!data) {
                    // nothing to do here
                    return [];
                }

                // handle case of non-object, which will not be searched
                if (data !== Object(data)) {
                    return [];
                }

                var results = [];

                // Handle array which we just traverse and recurse.
                if (data.constructor === Array) {
                    for (var i = 0, len = data.length; i < len; i++) {
                        results = results.concat(recursiveKeySearch(key, data[i]));
                    }
                    return results;
                }

                // We know we have an general object to work with now.
                // Now we need to iterate keys
                for (var dataKey in data) {
                    if (key === dataKey) {
                        // we found a match
                        results.push(data[key]);
                    }

                    // now recurse into value at key
                    results = results.concat(recursiveKeySearch(key, data[dataKey]));
                }

                return results;
            },
            ios = (function () {
                var ios = /^(?![\w\W]*Windows Phone)[\w\W]*(iPhone|iPad|iPod)/i.test(navigator.userAgent);
                if (ios) {
                    // tap anywhere on iOS to unfocus a submenu
                    $('html').css('cursor', 'pointer').on('click', $.noop);
                }
                return ios;
            })(),
            wp7 = (function () {
                var style = document.documentElement.style;
                return ('behavior' in style && 'fill' in style && /iemobile/i.test(navigator.userAgent));
            })(),
            unprefixedPointerEvents = (function () {
                return (!!w.PointerEvent);
            })(),
            toggleMenuClasses = function ($menu, o, add) {
                var classes = c.menuClass,
                    method;
                if (o.cssArrows) {
                    classes += ' ' + c.menuArrowClass;
                }
                method = (add) ? 'addClass' : 'removeClass';
                $menu[method](classes);
            },
            setPathToCurrent = function ($menu, o) {
                return $menu.find('li.' + o.pathClass).slice(0, o.pathLevels)
                    .addClass(o.hoverClass + ' ' + c.bcClass)
                    .filter(function () {
                        return ($(this).children(o.popUpSelector).hide().show().length);
                    }).removeClass(o.pathClass);
            },
            toggleAnchorClass = function ($li, add) {
                var method = (add) ? 'addClass' : 'removeClass';
                $li.children('a')[method](c.anchorClass);
            },
            toggleTouchAction = function ($menu) {
                var msTouchAction = $menu.css('ms-touch-action');
                var touchAction = $menu.css('touch-action');
                touchAction = touchAction || msTouchAction;
                touchAction = (touchAction === 'pan-y') ? 'auto' : 'pan-y';
                $menu.css({
                    'ms-touch-action': touchAction,
                    'touch-action': touchAction
                });
            },
            getMenu = function ($el) {
                return $el.closest('.' + c.menuClass);
            },
            getOptions = function ($el) {
                return getMenu($el).data('sfOptions');
            },
            over = function () {
                var $this = $(this),
                    o = getOptions($this);
                if (!o.disableHI) {
                    clearTimeout(o.sfTimer);
                    $this.siblings().superfish('hide').end().superfish('show');
                }
            },
            close = function (o) {
                o.retainPath = ($.inArray(this[0], o.$path) > -1);
                this.superfish('hide');

                if (!this.parents('.' + o.hoverClass).length) {
                    o.onIdle.call(getMenu(this));
                    if (o.$path.length) {
                        $.proxy(over, o.$path)();
                    }
                }
            },
            out = function () {
                var $this = $(this),
                    o = getOptions($this);
                if (ios) {
                    $.proxy(close, $this, o)();
                }
                else {
                    clearTimeout(o.sfTimer);
                    o.sfTimer = setTimeout($.proxy(close, $this, o), o.delay);
                }
            },
            touchHandler = function (e) {
                var $this = $(this),
                    o = getOptions($this),
                    $ul = $this.siblings(e.data.popUpSelector);

                if (o.onHandleTouch.call($ul) === false) {
                    return this;
                }

                if ($ul.length > 0 && $ul.is(':hidden')) {
                    $this.one('click.superfish', false);
                    if (e.type === 'MSPointerDown' || e.type === 'pointerdown') {
                        $this.trigger('focus');
                    } else {
                        $.proxy(over, $this.parent('li'))();
                    }
                }
            },
            applyHandlers = function ($menu, o) {
                var targets = 'li:has(' + o.popUpSelector + ')';
                if ($.fn.hoverIntent && !o.disableHI) {
                    $menu.hoverIntent(over, out, targets);
                }
                else {
                    $menu
                        .on('mouseenter.superfish', targets, over)
                        .on('mouseleave.superfish', targets, out);
                }
                var touchevent = 'MSPointerDown.superfish';
                if (unprefixedPointerEvents) {
                    touchevent = 'pointerdown.superfish';
                }
                if (!ios) {
                    touchevent += ' touchend.superfish';
                }
                if (wp7) {
                    touchevent += ' mousedown.superfish';
                }
                $menu
                    .on('focusin.superfish', 'li', over)
                    .on('focusout.superfish', 'li', out)
                    .on(touchevent, 'a', o, touchHandler);
            },
            createMenu = function (obj, el, id) {

                const submenus = [];
                let selectElement;
                // prüfen, ob Submenüs da sind
                for (let k in obj) {
                    if (obj.hasOwnProperty(k) && typeof obj[k] === 'object')
                        submenus.push(k)
                }

                let element = el;

                if (obj.title) {
                    const menuElement = $('<li>');

                    if (typeof obj.file === "boolean" && obj.file) {
                        selectElement = $('<label>');
                        selectElement.attr("for", "files_" + id);
                        const inputElement = $('<input>');
                        inputElement.attr("id", "files_" + id);
                        inputElement.attr("type", "file");
                        //inputElement.attr("multiple", "multiple");
                        inputElement.attr("style", "display:none;");
                        if (obj.action)
                            inputElement.on("change", obj.action );
                        menuElement.append(inputElement);

                    } else {
                        selectElement = $('<a>');
                    }
                    if (obj.className) {
                        selectElement.addClass(obj.className);
                        if (submenus.length > 0)
                            selectElement.addClass(c.anchorClass);
                    }
                    if (typeof obj.selected === "boolean" && obj.selected) {
                        selectElement.addClass("selected");
                    } else {
                        selectElement.removeClass("selected");
                    }
                    if (typeof obj.disabled === "boolean" && obj.disabled) {
                        selectElement.addClass("disableLI");
                    } else if (obj.action && !(typeof obj.file === "boolean" && obj.file)) {
                        if (typeof obj.autoClose === "boolean" && obj.autoClose) {
                            const action = obj.action;
                            obj.action = function (event) {
                                action(event);
                                event.data.root.superfish('hide');
                            };
                        }
                        selectElement.on("click", {root: getRootMenu()}, obj.action);
                    }

                    const textElement = $('<i>');
                    textElement.text(obj.title);
                    textElement.addClass("sf-entry");
                    selectElement.append(textElement);
                    selectElement.attr("id", id);

                    if (submenus.length > 0) {
                        const ulElement = $("<ul>");
                        if (typeof obj.disabled === "boolean" && obj.disabled)
                            ulElement.addClass("disableLI");
                        menuElement.append(ulElement);
                        menuElement.addClass("current");
                        element = ulElement;
                    }
                    menuElement.prepend(selectElement);
                    el.append(menuElement);
                }


                for (let i in submenus) {
                    createMenu(obj[submenus[i]], element, submenus[i]);
                }

            };

        return {
            // public methods
            hide: function (instant) {
                if (this.length) {
                    var $this = this,
                        o = getOptions($this);
                    if (!o) {
                        return this;
                    }
                    var not = (o.retainPath === true) ? o.$path : '',
                        $ul = $this.find('li.' + o.hoverClass).add(this).not(not).removeClass(o.hoverClass).children(o.popUpSelector),
                        speed = o.speedOut;

                    if (instant) {
                        $ul.show();
                        speed = 0;
                    }
                    o.retainPath = false;

                    if (o.onBeforeHide.call($ul) === false) {
                        return this;
                    }

                    $ul.stop(true, true).animate(o.animationOut, speed, function () {
                        var $this = $(this);
                        o.onHide.call($this);
                    });
                }
                return this;
            },
            show: function () {
                var o = getOptions(this);
                if (!o) {
                    return this;
                }
                var $this = this.addClass(o.hoverClass),
                    $ul = $this.children(o.popUpSelector);

                if ($ul.hasClass("disableLI"))
                    return this;

                if (o.onBeforeShow.call($ul) === false) {
                    return this;
                }

                $ul.stop(true, true).animate(o.animation, o.speed, function () {
                    o.onShow.call($ul);
                });
                return this;
            },
            destroy: function () {
                return this.each(function () {
                    var $this = $(this),
                        o = $this.data('sfOptions'),
                        $hasPopUp;
                    if (!o) {
                        return false;
                    }
                    $hasPopUp = $this.find(o.popUpSelector).parent('li');
                    clearTimeout(o.sfTimer);
                    toggleMenuClasses($this, o);
                    toggleAnchorClass($hasPopUp);
                    toggleTouchAction($this);
                    // remove event handlers
                    $this.off('.superfish').off('.hoverIntent');
                    // clear animation's inline display style
                    $hasPopUp.children(o.popUpSelector).attr('style', function (i, style) {
                        if (typeof style !== 'undefined') {
                            return style.replace(/display[^;]+;?/g, '');
                        }
                    });
                    // reset 'current' path classes
                    o.$path.removeClass(o.hoverClass + ' ' + c.bcClass).addClass(o.pathClass);
                    $this.find('.' + o.hoverClass).removeClass(o.hoverClass);
                    o.onDestroy.call($this);
                    $this.removeData('sfOptions');
                });
            },
            disable: function (id) {
                const element = $("#" + id);
                element.addClass("disableLI");
                element.parent().children("ul").addClass("disableLI");
                element.off("click");
            },
            enable: function (id) {
                const $this = $(this);
                const o = $this.data('sfOptions');
                const element = $(document.getElementById(id));
                element.removeClass("disableLI");
                element.parent().children("ul").removeClass("disableLI");
                if (o && o.menuData) {
                    const obj = recursiveKeySearch(id, o.menuData);
                    if (obj && obj.length && !obj[0].file && obj[0].action)
                        element.on("click", {root: getRootMenu()}, obj[0].action)
                }
            },
            select: function(id) {
                const element = $("#" + id);
                element.parent().parent().children("li").children("a").removeClass("selected");
                element.addClass("selected");
            },
            init: function (op) {
                return this.each(function () {
                    var $this = $(this);
                    setRootMenu($this);
                    if ($this.data('sfOptions')) {
                        return false;
                    }
                    var o = $.extend({}, $.fn.superfish.defaults, op),
                        $hasPopUp = $this.find(o.popUpSelector).parent('li');
                    if ($hasPopUp.length === 0) {
                        createMenu(o.menuData, $this, this.id);
                    }
                    o.$path = setPathToCurrent($this, o);

                    $this.data('sfOptions', o);

                    toggleMenuClasses($this, o, true);
                    toggleAnchorClass($hasPopUp, true);
                    toggleTouchAction($this);
                    applyHandlers($this, o);

                    $hasPopUp.not('.' + c.bcClass).superfish('hide', true);

                    o.onInit.call(this);
                });
            }
        };
    })();

    $.fn.superfish = function (method, args) {
        if (methods[method]) {
            return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
        }
        else if (typeof method === 'object' || !method) {
            return methods.init.apply(this, arguments);
        }
        else {
            return $.error('Method ' + method + ' does not exist on jQuery.fn.superfish');
        }
    };

    $.fn.superfish.defaults = {
        popUpSelector: 'ul,.sf-mega', // within menu context
        hoverClass: 'sfHover',
        pathClass: 'overrideThisToUse',
        pathLevels: 1,
        delay: 800,
        animation: {opacity: 'show'},
        animationOut: {opacity: 'hide'},
        speed: 'normal',
        speedOut: 'fast',
        cssArrows: true,
        disableHI: false,
        onInit: $.noop,
        onBeforeShow: $.noop,
        onShow: $.noop,
        onBeforeHide: $.noop,
        onHide: $.noop,
        onIdle: $.noop,
        onDestroy: $.noop,
        onHandleTouch: $.noop,
        menuData: []
    };

})(jQuery, window);