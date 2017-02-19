/**
 * Created by Klaus on 29.12.2014.
 */$.fn.dataTableExt.oPagination.paging_with_jqui_icons = {
    "fnInit": function (oSettings, nPaging, fnCallbackDraw) {
        var nFirst = document.createElement('span');
        var nPrevious = document.createElement('span');
        var nNext = document.createElement('span');
        var nLast = document.createElement('span');

        nFirst.innerHTML = "";
        nPrevious.innerHTML = "";
        nNext.innerHTML = "";
        nLast.innerHTML = "";

        nFirst.className = "fa fa-step-backward";
        nFirst.style.display = "inline-block";
        nFirst.style.paddingRight = "24px";
        nPrevious.className = "fa fa-play fa-rotate-180";
        nPrevious.style.display = "inline-block";
        nPrevious.style.paddingLeft = "24px";
        nNext.className = "fa fa-play";
        nNext.style.display = "inline-block";
        nNext.style.paddingRight = "24px";
        nLast.className = "fa fa-step-forward";
        nLast.style.display = "inline-block";

        if (oSettings.sTableId !== '') {
            nPaging.setAttribute('id', oSettings.sTableId + '_paginate');
            nFirst.setAttribute('id', oSettings.sTableId + '_first');
            nPrevious.setAttribute('id', oSettings.sTableId + '_previous');
            nNext.setAttribute('id', oSettings.sTableId + '_next');
            nLast.setAttribute('id', oSettings.sTableId + '_last');
        }

        nPaging.appendChild(nFirst);
        nPaging.appendChild(nPrevious);
        nPaging.appendChild(nNext);
        nPaging.appendChild(nLast);

        $(nFirst).click(function () {
            oSettings.oApi._fnPageChange(oSettings, "first");
            fnCallbackDraw(oSettings);
        });

        $(nPrevious).click(function () {
            oSettings.oApi._fnPageChange(oSettings, "previous");
            fnCallbackDraw(oSettings);
        });

        $(nNext).click(function () {
            oSettings.oApi._fnPageChange(oSettings, "next");
            fnCallbackDraw(oSettings);
        });

        $(nLast).click(function () {
            oSettings.oApi._fnPageChange(oSettings, "last");
            fnCallbackDraw(oSettings);
        });
    },

    "fnUpdate": function (oSettings, fnCallbackDraw) {
        if (!oSettings.aanFeatures.p)
            return;

        /* Loop over each instance of the pager. */
        var an = oSettings.aanFeatures.p;

        for (var i = 0, iLen = an.length; i < iLen; i++) {
            var icons = an[i].getElementsByTagName('span');

            if (oSettings._iDisplayStart === 0) {
                icons[0].style.display = "none";
                icons[1].style.display = "none";
            }
            else {
                icons[0].style.display = "inline-block";
                icons[1].style.display = "inline-block";
            }

            if (oSettings.fnDisplayEnd() == oSettings.fnRecordsDisplay()) {
                icons[2].style.display = "none";
                icons[3].style.display = "none";
            }
            else {
                icons[2].style.display = "inline-block";
                icons[3].style.display = "inline-block";
            }
        }
    }
};