define ([], function () {

    return function (data, view) {

        data = $('body').data ('data')

        $(w2ui ['passport_layout'].el ('main')).w2regrid ({

            name: 'account_common_log',

            show: {
                toolbar: true,
                toolbarInput: false,
                footer: true,
            },

            columnGroups : [
                {span: 3, caption: 'Событие'},
                {span: 4, caption: 'Платёж'},
                {span: 3, caption: 'Запрос в ГИС ЖКХ'},
            ],

            columns: [

                {field: 'ts', caption: 'Дата/время',    size: 30, render: _ts},
                {field: 'action', caption: 'Действие',    size: 30, voc: data.vc_actions},
                {field: 'vc_users.label', caption: 'Оператор/поставщик', size: 30, render: function (r) {return r ['tb_senders.label'] || r ['vc_users.label']}},

                {field: 'ordernum', caption: '№', size: 20},
                {field: 'orderdate', caption: 'Дата внесения платы',    size: 30, render: _dt},
                {field: 'amount', caption: 'Сумма, руб.', size: 20, render: 'float:2'},
                {field: 'paymentpurpose', caption: 'Назначение платежа', size: 50},

                {field: 'soap.ts', caption: 'Отправлено',    size: 30, render: _ts, attr: 'data-ref=1'},
                {field: 'soap.ts_rp', caption: 'Обработано',    size: 30, render: _ts, attr: 'data-ref=1'},
                {field: 'soap.err_text', caption: 'Ошибка',    size: 30},

            ],

            url: '/_back/?type=payments&part=acknowledgments&id=' + $_REQUEST.id,

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