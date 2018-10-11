define ([], function () {

    function _ddt (record, ind, col_ind, data) {
        return data < 99 ? data : 'посл.'
    }
    
    var nxt = {
        0: 'тек.',
        1: 'след.',
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
                {span: 5, caption: 'Событие'},
                {span: 17, caption: 'Значения полей'},
                {span: 4, caption: 'Запрос в ГИС ЖКХ'},
            ], 
            
            columns: [                
                {field: 'ts', caption: 'Дата/время',    size: 30, render: _ts},
                {field: 'vc_users.label', caption: 'Оператор',    size: 30},
                {field: 'action', caption: 'Действие',    size: 30, voc: data.vc_actions},
                {field: 'id_ctr_status_gis',  caption: 'Статус до',     size: 10, voc: data.vc_gis_status},
                {field: 'id_ctr_status_gis_next',  caption: 'Статус после',     size: 10, voc: data.vc_gis_status},

                {field: 'docnum', caption: 'Номер', size: 20},
                {field: 'signingdate', caption: 'Дата заключения', size: 18, render: _dt},
                {field: 'org.label', caption: 'Исполнитель', size: 100, off: $_USER.role.nsi_20_1},               
                {field: 'id_customer_type',  caption: 'Тип заказчика',     size: 30, voc: data.vc_gis_customer_type},                
                {field: 'org_customer.label', caption: 'Заказчик', size: 100},
                {field: 'contractbase',  caption: 'Основание заключения',     size: 30, voc: data.vc_nsi_58},                
                {field: 'effectivedate', caption: 'Дата вступления в силу', size: 18, render: _dt},                
                {field: 'plandatecomptetion', caption: 'Дата окончания', size: 18, render: _dt},                               
                {field: 'ddt_m_start', caption: 'Ввод ПУ с (дата)', size: 5, render: _ddt, hidden: true},
                {field: 'ddt_m_start_nxt', caption: 'Ввод ПУ с (мес.)', size: 5, voc: nxt, hidden: true},
                {field: 'ddt_m_end', caption: 'Ввод ПУ по (дата)', size: 5, render: _ddt, hidden: true},
                {field: 'ddt_m_end_nxt', caption: 'Ввод ПУ по (мес.)', size: 5, voc: nxt, hidden: true},
                {field: 'ddt_d_start', caption: 'Плат. док. (дата)', size: 5, render: _ddt, hidden: true},
                {field: 'ddt_d_start_nxt', caption: 'Плат. док. (мес.)', size: 5, voc: nxt, hidden: true},
                {field: 'ddt_i_start', caption: 'Внес. платы (дата)', size: 5, render: _ddt, hidden: true},
                {field: 'ddt_i_start_nxt', caption: 'Внес. платы (мес.)', size: 5, voc: nxt, hidden: true},
                {field: 'is_deleted',  caption: 'Статус',     size: 20, voc: {0: 'Актуально', 1: 'Удалено'}},
                
                {field: 'soap.ts', caption: 'Отправлено',    size: 30, render: _ts, attr: 'data-ref=1'},
                {field: 'soap.ts_rp', caption: 'Обработано',    size: 30, render: _ts, attr: 'data-ref=1'},
                {field: 'soap.id_status', caption: 'Статус',    size: 30, render: function (r, i, c, v) {return (
                    r.action != 'approve' ? '' : 
                    r ['soap.ts_rp']      ? 'Обработано' : 
                    r ['soap.ts']         ? 'Ожидает ответа' : 
                                            'Ожидает отправки'
                )}},
                {field: 'soap.err_text', caption: 'Ошибка',    size: 30},

            ],
            
            url: '/mosgis/_rest/?type=mgmt_contracts&part=log&id=' + $_REQUEST.id,
            
            onClick: function (e) {
            
                var c = this.columns [e.column]
                var r = this.get (e.recid)
                
                switch (c.field) {
                    case 'soap.ts':    if (r.uuid_out_soap) return openTab ('/out_soap_rq/' + r.uuid_out_soap)
                    case 'soap.ts_rp': if (r.uuid_message) return openTab ('/out_soap_rp/' + r.uuid_message)
                }
            
            },
            
            onLoad: function (e) {

                if (e.xhr.status != 200) return $_DO.apologize ({jqXHR: e.xhr})

                var content = JSON.parse (e.xhr.responseText).content

                var data = {
                    status : "success",
                    total  : content.cnt
                }

                delete content.cnt
                delete content.portion

                for (key in content) {    
                
                    var rs = dia2w2uiRecords (content [key])
                    
                    var len = rs.length
                    
                    if (len) rs [0].id_ctr_status_gis_next = $('body').data ('data').item.id_ctr_status_gis
                    
                    for (var i = 1; i < len; i ++) rs [i].id_ctr_status_gis_next = rs [i - 1].id_ctr_status_gis
                    
                    function fix (r, f) {if (r [f] == 80) r [f] = 10}
                    
                    $.each (rs, function () {
                        if (this.action == 'create') delete this.id_ctr_status_gis
                        fix (this, 'id_ctr_status_gis')
                        fix (this, 'id_ctr_status_gis_next')
                    })
                    
                    data.records = rs

                    e.xhr.responseText = JSON.stringify (data)
                    
                }

            }            

        }).refresh ();

    }

})