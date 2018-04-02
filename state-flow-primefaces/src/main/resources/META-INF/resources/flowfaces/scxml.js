if (!PrimeFaces.scxml) {

    PrimeFaces.scxml = {

        openScxmlDialog: function (cfg) {
            cfg.options.closable = false;
            cfg.options.resizable = false;
            
            PrimeFaces.dialog.DialogHandler.openDialog(cfg);
        },
        closeScxmlDialog: function (cfg) {
            PrimeFaces.dialog.DialogHandler.closeDialog(cfg);
        },

    };

    PrimeFaces.widget.ScxmlDialogInvoker = PrimeFaces.widget.BaseWidget.extend({

        init: function (cfg) {
            this._super(cfg);

            this.cfg.formId = $(this.jqId).closest('form').attr('id');
            this.content = $(this.jqId + '_content');

        }

    });

}
