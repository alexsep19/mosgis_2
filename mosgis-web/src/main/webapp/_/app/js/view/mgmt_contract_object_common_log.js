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
                {span: 3, caption: 'Событие'},
                {span: 3, caption: 'Значения полей'},
                {span: 4, caption: 'Запрос в ГИС ЖКХ'},
            ], 
            
            columns: [                
                {field: 'ts', caption: 'Дата/время',    size: 30, render: _ts},
                {field: 'action', caption: 'Действие',    size: 30, voc: {
                    alter:    'Изменение',
                    approve:  'Утверждение',
                    create:   'Создание',
                    update:   'Редактирование',
                    delete:   'Удаление',
                    undelete: 'Восстановление',
                }},
                {field: 'vc_users.label', caption: 'Оператор',    size: 30},

                {field: 'startdate', caption: 'Начало', size: 18, render: _dt},
                {field: 'enddate', caption: 'Окончание', size: 18, render: _dt},
                {field: '_', caption: 'Основание', size: 50, render: function (r) {
                    return r.uuid_contract_agreement ? r ["contract_agreement.label"] : 'договор'
                }},
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
            
            url: '/mosgis/_rest/?type=contract_objects&part=log&id=' + $_REQUEST.id,            
            
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