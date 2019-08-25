if (!PrimeFaces.scxml) {

    PrimeFaces.scxml = {

        open: [],
        close: [],
        change: [],

        openScxmlDialog: function (cfg) {
            var rootWindow = this.findRootWindow();
            rootWindow.PrimeFaces.scxml.open.push(cfg);
            rootWindow.PrimeFaces.scxml.invokeLater();
        },

        closeScxmlDialog: function (cfg) {
            var rootWindow = this.findRootWindow();
            rootWindow.PrimeFaces.scxml.close.push(cfg);
            rootWindow.PrimeFaces.scxml.invokeLater();
        },

        changeScxmlDialog: function (cfg) {
            var rootWindow = this.findRootWindow();
            rootWindow.PrimeFaces.scxml.change.push(cfg);
            rootWindow.PrimeFaces.scxml.invokeLater();
        },

        invokeLater: function () {
            var rootWindow = this.findRootWindow();
            if (!rootWindow.PrimeFaces.scxml.signal) {
                rootWindow.PrimeFaces.scxml.signal = setTimeout(rootWindow.PrimeFaces.scxml.invoke, 100);
            }
        },

        invoke: function () {
            var rootWindow = PrimeFaces.scxml.findRootWindow();
            var closeCfg;
            var dlg, parentDlg;

            clearTimeout(PrimeFaces.scxml.signal);
            PrimeFaces.scxml.signal = null;

            PrimeFaces.scxml.close.forEach(function (cfg) {
                var dlgs = $(rootWindow.document.body).children('div.ui-dialog[data-pfdlgcid="' + cfg.pfdlgcid + '"]').not('[data-queuedforremoval]');
                var dlgsLength = dlgs.length;
                dlg = dlgs.eq(dlgsLength - 1);
                parentDlg = dlgsLength > 1 ? dlgs.eq(dlgsLength - 2) : null;
                closeCfg = cfg;
                PrimeFaces.dialog.DialogHandler.closeDialog(cfg);
            });

            PrimeFaces.scxml.open.forEach(function (cfg) {
                if (closeCfg) {
                    closeCfg = null;
                }

                var dlgs = $(rootWindow.document.body).children('div.ui-dialog[data-pfdlgcid="' + cfg.pfdlgcid + '"]').not('[data-queuedforremoval]');
                var dlgsLength = dlgs.length;
                parentDlg = dlgsLength > 0 ? dlgs.eq(dlgsLength - 1) : null;
                if (parentDlg) {
                    var parentDlgFrame = parentDlg.find('> .ui-dialog-content > iframe').get(0);
                    windowContext = parentDlgFrame.contentWindow || parentDlgFrame;
                } else {
                    windowContext = rootWindow;
                }

                var rootWidgetVar = cfg.pfdlgcid.replace(/:/g, '_') + '_scxmldlgrootwidget';
                if (!windowContext.PrimeFaces.widgets[rootWidgetVar]) {
                    PrimeFaces.cw.call(windowContext.PrimeFaces, 'ScxmlDialogRoot', rootWidgetVar, {
                        id: cfg.pfdlgcid + ':' + cfg.invokeId,
                        sourceComponentId: cfg.sourceComponentId,
                        behaviors: cfg.behaviors
                    });
                }

                if (!cfg.sourceWidgetVar) {
                    cfg.sourceWidgetVar = cfg.sourceComponentId.replace(/:/g, '_') + '_scxmldlgwidget';
                    PrimeFaces.cw.call(windowContext.PrimeFaces, 'ScxmlDialogInvoker', cfg.sourceWidgetVar, {
                        id: cfg.invokeId,
                    });
                }

                PrimeFaces.dialog.DialogHandler.openDialog(cfg);
            });
            
            PrimeFaces.scxml.change.forEach(function (cfg) {
                var dlgs = $(rootWindow.document.body).children('div.ui-dialog[data-pfdlgcid="' + cfg.pfdlgcid + '"]').not('[data-queuedforremoval]');
                var dlgsLength = dlgs.length;
                var cdlg = dlgs.eq(dlgsLength - 1);

                if (cdlg) {
                    var dialogFrame = cdlg.find('iframe');

                    if (cfg.options.contentWidth) {
                        var frameWidth = cfg.options.contentWidth || 640;
                        dialogFrame.width(frameWidth);
                    }

                    if (cfg.options.iframeTitle) {
                        dialogFrame.attr('title', cfg.options.iframeTitle);
                    }

                    if (cfg.url) {
                        var symbol = cfg.url.indexOf('?') === -1 ? '?' : '&';
                        var frameURL = cfg.url.indexOf('pfdlgcid') === -1 ? cfg.url + symbol + 'pfdlgcid=' + cfg.pfdlgcid : cfg.url;
                        dialogFrame.attr('src', frameURL);
                    }
                }

            });
            
            
            PrimeFaces.scxml.close = [];
            PrimeFaces.scxml.open = [];
            PrimeFaces.scxml.change = [];

            if (closeCfg) {
                var sourceComponentId;
                var dialogReturnBehavior = null;
                var windowContext = null;

                var rootWidgetVar = closeCfg.pfdlgcid.replace(/:/g, '_') + '_scxmldlgrootwidget';

                if (parentDlg) {
                    var parentDlgFrame = parentDlg.find('> .ui-dialog-content > iframe').get(0);
                    windowContext = parentDlgFrame.contentWindow || parentDlgFrame;
                } else {
                    windowContext = rootWindow;
                }
                var rootWidget = windowContext.PF(rootWidgetVar);

                dialogReturnBehavior = rootWidget.cfg.behaviors ? rootWidget.cfg.behaviors['parentRefresh'] : null;
                sourceComponentId = rootWidget.cfg.sourceComponentId;

                if (dialogReturnBehavior) {
                    var ext = {
                        params: [
                            {name: sourceComponentId + '_pfdlgcid', value: closeCfg.pfdlgcid}
                        ]
                    };

                    dialogReturnBehavior.call(windowContext, ext);
                }
            }


        },

        findRootWindow: function () {
            var w = window;
            while (w.frameElement) {
                var parent = w.parent;
                if (parent.PF === undefined) {
                    break;
                }
                w = parent;
            }
            ;

            return w;
        }

    };

    PrimeFaces.widget.ScxmlDialogInvoker = PrimeFaces.widget.BaseWidget.extend({

        init: function (cfg) {
            this._super(cfg);

            this.cfg.formId = $(this.jqId).closest('form').attr('id');
            this.content = $(this.jqId + '_content');

        }

    });

    PrimeFaces.widget.ScxmlDialogRoot = PrimeFaces.widget.BaseWidget.extend({

        init: function (cfg) {
            this._super(cfg);

            this.cfg.formId = $(this.jqId).closest('form').attr('id');
            this.content = $(this.jqId + '_content');

        }

    });


}

