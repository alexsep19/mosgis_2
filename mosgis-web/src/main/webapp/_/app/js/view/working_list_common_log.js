define ([], function () {

    function _my (x, y, z, t) {
        var p = t.split ('-')
        return w2utils.settings.fullmonths [parseInt (p [1]) - 1] + ' ' + p [0]
    }

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: 'mgmt_contract_common_log',

            show: {
                toolbar: true,
                toolbarInput: false,
                footer: true,
            },     

            columnGroups : [
                {span: 3, caption: 'Событие'},
                {span: 3, caption: 'Значения полей'},
//                {span: 4, caption: 'Запрос в ГИС ЖКХ'},
            ], 
            
            columns: [                
                {field: 'ts', caption: 'Дата/время',    size: 30, render: _ts},
                {field: 'action', caption: 'Действие',    size: 30, voc: data.vc_actions},
                {field: 'vc_users.label', caption: 'Оператор',    size: 30},

                {field: 'dt_from', caption: 'Начало', size: 20, render: _my},
                {field: 'dt_to', caption: 'Окончание', size: 20, render: _my},
                {field: 'id_ctr_status', caption: 'Статус', size: 100, voc: data.vc_gis_status},
/*                
                {field: 'soap.ts', caption: 'Отправлено',    size: 30, render: _ts, attr: 'data-ref=1'},
                {field: 'soap.ts_rp', caption: 'Обработано',    size: 30, render: _ts, attr: 'data-ref=1'},
                {field: 'soap.id_status', caption: 'Статус',    size: 30, render: function (r, i, c, v) {return (
                    r.action != 'approve' ? '' : 
                    r ['soap.ts_rp']      ? 'Обработано' : 
                    r ['soap.ts']         ? 'Ожидает ответа' : 
                                            'Ожидает отправки'
                )}},
                {field: 'soap.err_text', caption: 'Ошибка',    size: 30},
*/
            ],
            
            url: '/mosgis/_rest/?type=working_lists&part=log&id=' + $_REQUEST.id,            
/*            
            onClick: function (e) {
            
                var c = this.columns [e.column]
                var r = this.get (e.recid)
                
                switch (c.field) {
                    case 'soap.ts':    if (r.uuid_out_soap) return openTab ('/out_soap_rq/' + r.uuid_out_soap)
                    case 'soap.ts_rp': if (r.uuid_message) return openTab ('/out_soap_rp/' + r.uuid_message)
                }
            
            }
*/
        }).refresh ();

    }

})