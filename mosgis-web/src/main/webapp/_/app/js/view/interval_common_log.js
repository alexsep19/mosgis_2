define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')

        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: 'interval_common_log',

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

                {field: 'code_vc_nsi_3', caption: 'Вид коммунальной услуги', size: 20, voc: data.vc_nsi_3 },
                {field: 'code_vc_nsi_239', caption: 'Вид коммунального ресурса', size: 20, voc: data.vc_nsi_239 },
                {field: 'startdateandtime', caption: 'Начала перерыва', size: 20, render: _dt},
                {field: 'enddateandtime', caption: 'Окончание перерыва', size: 20, render: _dt},

                {field: 'soap.ts', caption: 'Отправлено',    size: 30, render: _ts, attr: 'data-ref=1'},
                {field: 'soap.ts_rp', caption: 'Обработано',    size: 30, render: _ts, attr: 'data-ref=1'},
                {field: 'soap.err_text', caption: 'Ошибка',    size: 30},

            ],
            
            url: '/mosgis/_rest/?type=intervals&part=log&id=' + $_REQUEST.id,
            
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