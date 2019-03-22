if (PrimeFaces.ajax) {

    PrimeFaces.ajax.Utils = {

        loadStylesheets: function (stylesheets) {
            for (var i = 0; i < stylesheets.length; i++) {
                $('head').append('<link type="text/css" rel="stylesheet" href="' + stylesheets[i] + '" />');
            }
        },

        loadScripts: function (scripts) {
            var loadNextScript = function () {
                var script = scripts.shift();
                if (script) {
                    PrimeFaces.getScript(script, loadNextScript);
                }
            };

            loadNextScript();
        },

        getContent: function (node) {
            var content = '';

            for (var i = 0; i < node.childNodes.length; i++) {
                content += node.childNodes[i].nodeValue;
            }

            return content;
        },

        updateFormStateInput: function (name, value, xhr) {
            var trimmedValue = $.trim(value);

            var forms = null;
            if (xhr && xhr.pfSettings && xhr.pfSettings.portletForms) {
                forms = $(xhr.pfSettings.portletForms);
            } else {
                forms = $('form');
            }

            var parameterPrefix = '';
            if (xhr && xhr.pfArgs && xhr.pfArgs.parameterPrefix) {
                parameterPrefix = xhr.pfArgs.parameterPrefix;
            }

            for (var i = 0; i < forms.length; i++) {
                var form = forms.eq(i);

                if (form.attr('method') === 'post') {
                    var input = form.children("input[name='" + parameterPrefix + name + "']");

                    if (input.length > 0) {
                        input.val(trimmedValue);
                    } else {
                        form.append('<input type="hidden" name="' + parameterPrefix + name + '" value="' + trimmedValue + '" autocomplete="off" />');
                    }
                }
            }
        },

        updateHead: function (content) {
            var cache = $.ajaxSetup()['cache'];
            $.ajaxSetup()['cache'] = true;

            var headStartTag = new RegExp("<head[^>]*>", "gi").exec(content)[0];
            var headStartIndex = content.indexOf(headStartTag) + headStartTag.length;
            $('head').html(content.substring(headStartIndex, content.lastIndexOf("</head>")));

            $.ajaxSetup()['cache'] = cache;
        },

        updateBody: function (content) {
            var bodyStartTag = new RegExp("<body[^>]*>", "gi").exec(content)[0];
            var bodyStartIndex = content.indexOf(bodyStartTag) + bodyStartTag.length;
            $('body').html(content.substring(bodyStartIndex, content.lastIndexOf("</body>")));
        },

        updateElement: function (id, content, xhr) {

            if (id.indexOf(PrimeFaces.VIEW_STATE) !== -1) {
                PrimeFaces.ajax.Utils.updateFormStateInput(PrimeFaces.VIEW_STATE, content, xhr);
            } else if (id.indexOf(PrimeFaces.CLIENT_WINDOW) !== -1) {
                PrimeFaces.ajax.Utils.updateFormStateInput(PrimeFaces.CLIENT_WINDOW, content, xhr);
            }
            // used by @all
            else if (id === PrimeFaces.VIEW_ROOT) {

                // backup our utils, we reset it soon
                var ajaxUtils = PrimeFaces.ajax.Utils;

                // reset PrimeFaces JS state because the view is completely replaced with a new one
                //window.PrimeFaces = null;

                //ajaxUtils.updateHead(content);
                ajaxUtils.updateBody(content);
            } else if (id === PrimeFaces.ajax.VIEW_HEAD) {
                PrimeFaces.ajax.Utils.updateHead(content);
            } else if (id === PrimeFaces.ajax.VIEW_BODY) {
                PrimeFaces.ajax.Utils.updateBody(content);
            } else if (id === PrimeFaces.ajax.RESOURCE) {
                $('head').append(content);
            } else if (id === $('head')[0].id) {
                PrimeFaces.ajax.Utils.updateHead(content);
            } else {
                $(PrimeFaces.escapeClientId(id)).replaceWith(content);
            }
        }
    }

}