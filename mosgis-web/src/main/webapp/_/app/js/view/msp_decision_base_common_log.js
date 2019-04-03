define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')

        data.voc_bool = {
            0 : "Нет",
            1 : "Да"
        }

        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: 'msp_decision_base_common_log',

            show: {
                toolbar: true,
                toolbarInput: false,
                footer: true,
            },     

            columnGroups : [
                {span: 3, caption: 'Событие'},
                {span: 5, caption: 'Значения полей'},
                {span: 3, caption: 'Запрос в ГИС ЖКХ'},
            ], 
            
            columns: [                
                {field: 'ts', caption: 'Дата/время',    size: 30, render: _ts},
                {field: 'action', caption: 'Действие',    size: 30, voc: data.vc_actions},
                {field: 'vc_users.label', caption: 'Оператор',    size: 30},

                {field: 'uniqunumber',  caption: 'Код',     size: 20},
                {field: 'decisionname', caption: 'Наименование', size: 50},
                {field: 'code_vc_nsi_301',  caption: 'Тип',     size: 25, voc: data.vc_nsi_301},
                {field: 'isappliedtosubsidiaries', caption: 'Для субсидий', size: 25, voc: data.voc_bool},
                {field: 'isappliedtorefundofcharges', caption: 'Для расходов', size: 25, voc: data.voc_bool},
                
                {field: 'soap.ts', caption: 'Отправлено',    size: 30, render: _ts, attr: 'data-ref=1'},
                {field: 'soap.ts_rp', caption: 'Обработано',    size: 30, render: _ts, attr: 'data-ref=1'},
                {field: 'soap.err_text', caption: 'Ошибка',    size: 30},

            ],
            
            url: '/mosgis/_rest/?type=msp_decision_bases&part=log&id=' + $_REQUEST.id,
            
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