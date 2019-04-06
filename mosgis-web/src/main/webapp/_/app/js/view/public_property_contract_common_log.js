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

            name: 'public_property_contract_common_log',

            show: {
                toolbar: true,
                toolbarInput: false,
                footer: true,
            },     

            columnGroups : [
                {span: 5, caption: 'Событие'},
                {span: 14, caption: 'Значения полей'},
                {span: 3, caption: 'Запрос в ГИС ЖКХ'},
            ], 
            
            columns: [                
                {field: 'ts', caption: 'Дата/время',    size: 30, render: _ts},
                {field: 'vc_users.label', caption: 'Оператор',    size: 30},
                {field: 'action', caption: 'Действие',    size: 30, voc: data.vc_actions},
                {field: 'id_ctr_status_gis',  caption: 'Статус до',     size: 10, voc: data.vc_gis_status},
                {field: 'id_ctr_status_gis_next',  caption: 'Статус после',     size: 10, voc: data.vc_gis_status},

                {field: 'contractnumber', caption: 'Номер', size: 20},
                {field: 'date_', caption: 'Дата заключения', size: 18, render: _dt},
                {field: 'startdate', caption: 'Дата вступления в силу', size: 18, render: _dt},
                {field: 'enddate', caption: 'Дата окончания', size: 18, render: _dt},               
                {field: 'contractobject', caption: 'Предмет', size: 30},
                {field: 'comments', caption: 'Комментарии', size: 30},
                {field: 'isgratuitousbasis', caption: 'Условия использования', size: 10, voc: {0: 'Возмездное', 1: 'Безвозмездное'}},
                {field: 'payment', caption: 'Оплата', size: 10},
                {field: 'ddt_start', caption: 'Оплата с (дата)', size: 5, render: _ddt, hidden: true},
                {field: 'ddt_start_nxt', caption: 'Оплата с (мес.)', size: 5, voc: nxt, hidden: true},
                {field: 'ddt_end', caption: 'Оплата по (дата)', size: 5, render: _ddt, hidden: true},
                {field: 'ddt_end_nxt', caption: 'Оплата по (мес.)', size: 5, voc: nxt, hidden: true},
                {field: 'other', caption: 'Иное', size: 30, hidden: true},
                {field: 'moneyspentdirection', caption: 'Направление расходования', size: 30, hidden: true},
                
                {field: 'soap.ts', caption: 'Отправлено',    size: 30, render: _ts, attr: 'data-ref=1'},
                {field: 'soap.ts_rp', caption: 'Обработано',    size: 30, render: _ts, attr: 'data-ref=1'},
                {field: 'soap.err_text', caption: 'Ошибка',    size: 30},

            ],
            
            url: '/_back/?type=public_property_contracts&part=log&id=' + $_REQUEST.id,
            
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