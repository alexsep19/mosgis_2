define ([], function () {

    return function (data, view) {

        data = $('body').data ('data')
        
        var it = data.item

        $(w2ui ['passport_layout'].el ('main')).w2regrid ({

            name: 'account_common_log',

            show: {
                toolbar: true,
                toolbarAdd: it._can.edit_acknowledgments,
                toolbarInput: false,
                footer: true,
            },
/*
            columnGroups : [
                {span: 4, caption: 'Платёж'},
            ],
*/
            columns: [

                {field: 'acct.accountnumber', caption: 'Номер лицевого счета',    size: 30},
                {field: 'pd.paymentdocumentnumber', caption: 'Платежный документ',    size: 30},
                {field: 'pd.id_type', caption: 'Тип',    size: 30, voc: data.vc_pay_doc_types},                
                {field: 'pd.dt_period', caption: 'Период', size: 22, render: function (r, y, z, v) {
                    var dt = new Date (v)
                    return w2utils.settings.fullmonths [dt.getMonth ()] + ' ' + dt.getFullYear ()
                }},
                {field: 'pd.totalpayablebypdwith_da', caption: 'Сумма документа, руб.', size: 15, render: 'float:2'},
                {field: 'amount', caption: 'Оплачено', size: 15, render: 'float:2'},

            ],

            url: '/_back/?type=payments&part=acknowledgments&id=' + $_REQUEST.id,

            onAdd: $_DO.create_payment_acknowledgments,

            onClick: function (e) {

                var c = this.columns [e.column]
                var r = this.get (e.recid)

                switch (c.field) {
                    case 'soap.ts':    if (r.uuid_out_soap) return openTab ('/out_soap_rq/' + r.uuid_out_soap)
                    case 'soap.ts_rp': if (r.uuid_message) return openTab ('/out_soap_rp/' + r.uuid_message)
                }

            }

        }).refresh ();

    }

})