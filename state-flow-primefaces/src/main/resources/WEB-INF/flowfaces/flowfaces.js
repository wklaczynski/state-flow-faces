(function (window) {
    FacesFlowUI = {

        openSCXMLDialog: function (cfg) {
            PrimeFaces.scxml.DialogHandler.openDialog(cfg);
        },
        closeSCXMLDialog: function (cfg) {
            PrimeFaces.scxml.DialogHandler.closeDialog(cfg);
        },
    };

    window.FacesFlowUI = FacesFlowUI;

    PrimeFaces.scxml = {};
    PrimeFaces.scxml.DialogHandler = {
        openDialog: function (cfg) {
            var dialogId = cfg.sourceId + '_dlg';
            if (document.getElementById(dialogId)) {
                return;
            }

            var dialogWidgetVar = cfg.sourceId.replace(/:/g, '_') + '_dlgwidget',
                    dialogDOM = $('<div id="' + dialogId + '" class="ui-dialog ui-widget ui-widget-content ui-corner-all ui-shadow ui-hidden-container ui-overlay-hidden"' +
                            ' data-pfdlgcid="' + cfg.pfdlgcid + '" data-widgetvar="' + dialogWidgetVar + '"></div>')
                    .append('<div class="ui-dialog-titlebar ui-widget-header ui-helper-clearfix ui-corner-top"><span class="ui-dialog-title"></span></div>');
            if (cfg.options.closable !== false) {
                dialogDOM.children('.ui-dialog-titlebar')
                        .append('<a class="ui-dialog-titlebar-icon ui-dialog-titlebar-close ui-corner-all" href="#" role="button"><span class="ui-icon ui-icon-closethick"></span></a>');
            }

            dialogDOM.append('<div class="ui-dialog-content ui-widget-content" style="height: auto;">' +
                    '<iframe style="border:0 none" frameborder="0"/>' +
                    '</div>');
            dialogDOM.appendTo(document.body);
            var dialogFrame = dialogDOM.find('iframe'),
                    symbol = cfg.url.indexOf('?') === -1 ? '?' : '&',
                    frameURL = cfg.url + symbol + 'pfdlgcid=' + cfg.pfdlgcid,
                    frameWidth = cfg.options.contentWidth || 640;
            dialogFrame.width(frameWidth);
            dialogFrame.on('load', function () {
                var $frame = $(this),
                        titleElement = $frame.contents().find('title');
                if (!$frame.data('initialized')) {
                    PrimeFaces.cw('DynamicDialog', dialogWidgetVar, {
                        id: dialogId,
                        position: 'center',
                        sourceId: cfg.sourceId,
                        behaviors: cfg.behaviors,
                        onHide: function () {
                            var $dialogWidget = this,
                                    dialogFrame = this.content.children('iframe');
                            PrimeFaces.scxml.DialogHandler.handleDialogReturnEvent(cfg);
                            if (dialogFrame.get(0).contentWindow.PrimeFaces) {
                                this.destroyIntervalId = setInterval(function () {
                                    if (dialogFrame.get(0).contentWindow.PrimeFaces.ajax.Queue.isEmpty()) {
                                        clearInterval($dialogWidget.destroyIntervalId);
                                        dialogFrame.attr('src', 'about:blank');
                                        $dialogWidget.jq.remove();
                                    }
                                }, 10);
                            } else {
                                dialogFrame.attr('src', 'about:blank');
                                $dialogWidget.jq.remove();
                            }

                            PF[dialogWidgetVar] = undefined;
                        },
                        modal: cfg.options.modal,
                        resizable: cfg.options.resizable,
                        draggable: cfg.options.draggable,
                        width: cfg.options.width,
                        height: cfg.options.height
                    });
                }

                if (titleElement.length > 0) {
                    PF(dialogWidgetVar).titlebar.children('span.ui-dialog-title').html(titleElement.text());
                }

                //adjust height
                var offset = PrimeFaces.env.browser.webkit ? 5 : 20,
                        frameHeight = cfg.options.contentHeight || $frame.get(0).contentWindow.document.body.scrollHeight + offset;
                $frame.height(frameHeight);
                var lastHeight = frameHeight;
                if (!cfg.options.contentHeight) {
                    var iframe = this;
                    $frame.css({overflow: 'hidden'});
                    var autoResize = function () {
                        if (!iframe.contentWindow) {
                            return;
                        }
                        frameHeight = Math.max(
                                $frame.get(0).contentWindow.document.body.scrollHeight,
                                $frame.get(0).contentWindow.document.body.offsetHeight);
                        if (frameHeight !== lastHeight) {
                            lastHeight = frameHeight;
                            $frame.height(frameHeight);
                        }
                        setTimeout(autoResize, 500);
                    };
                    autoResize();
                }


                dialogFrame.data('initialized', true);
                PF(dialogWidgetVar).show();
            }).attr('src', frameURL);
        },
        closeDialog: function (cfg) {
            var dlg = $(document.body).children('div.ui-dialog').filter(function () {
                return $(this).data('pfdlgcid') === cfg.pfdlgcid;
            }),
                    dlgWidget = PF(dlg.data('widgetvar'));
            dlgWidget.hide();
        },
        handleDialogReturnEvent: function (cfg) {
            var dlg = $(document.body).children('div.ui-dialog').filter(function () {
                return $(this).data('pfdlgcid') === cfg.pfdlgcid;
            });
            var dlgWidget = PF(dlg.data('widgetvar'));
            var sourceId = dlgWidget.cfg.sourceId;
            var dialogReturnBehavior = null;
            if (dlgWidget.cfg.behaviors) {
                dialogReturnBehavior = dlgWidget.cfg.behaviors['scxmlHide'];
            }

            if (dialogReturnBehavior) {
                var ext = {
                    params: [
                        {name: sourceId + '_pfdlgcid', value: cfg.pfdlgcid}
                    ]
                };
                dialogReturnBehavior.call(this, ext);
            }
        }
    };


})(window);
