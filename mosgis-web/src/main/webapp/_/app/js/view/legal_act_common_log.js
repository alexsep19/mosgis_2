define ([], function () {

    return function (data, view) {

        data = $('body').data ('data')

        $(w2ui ['passport_layout'].el ('main')).w2regrid ({

            name: 'legal_act_common_log',

            show: {
                toolbar: true,
                toolbarInput: false,
                footer: true,
            },

            columnGroups : [
                {span: 3, caption: 'Событие'},
                {span: 4, caption: 'Значения полей'},
                {span: 3, caption: 'Запрос в ГИС ЖКХ'},
            ],

            columns: [
                {field: 'ts', caption: 'Дата/время',    size: 30, render: _ts},
                {field: 'action', caption: 'Действие',    size: 30, voc: data.vc_actions},
                {field: 'vc_users.label', caption: 'Оператор',    size: 30},

                {field: 'code_vc_nsi_324', caption: 'Вид документа', size: 50, voc: data.vc_nsi_324 },
                {field: 'docnumber', caption: 'Номер', size: 20},
                {field: 'name', caption: 'Наименование', size: 50},
                {field: 'approvedate', caption: 'Дата принятия ОГВ', size: 25, render: _dt},

                {field: 'soap.ts', caption: 'Отправлено',    size: 30, render: _ts, attr: 'data-ref=1'},
                {field: 'soap.ts_rp', caption: 'Обработано',    size: 30, render: _ts, attr: 'data-ref=1'},
                {field: 'soap.err_text', caption: 'Ошибка',    size: 30},

            ],

            url: '/_back/?type=legal_acts&part=log&id=' + $_REQUEST.id,

            onClick: function (e) {

                var c = this.columns [e.column]
                var r = this.get (e.recid)

                switch (c.field) {
                    case 'soap.ts':    if (r.uuid_out_soap) return openTab ('/out_soap_rq/' + r.uuid_out_soap)
                    case 'soap.ts_rp': if (r['soap.uuid_ack']) return openTab ('/out_soap_rp/' + r['soap.uuid_ack'])
                }

            }

        }).refresh ();

    }

})