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

            name: 'charter_common_log',

            show: {
                toolbar: true,
                toolbarInput: false,
                footer: true,
            },

            columnGroups : [
                {span: 3, caption: 'Событие'},
                {span: 4, caption: 'Значения полей'},
                {span: 4, caption: 'Запрос в ГИС ЖКХ'},
            ], 
            
            columns: [
                {field: 'ts', caption: 'Дата/время',    size: 30, render: _ts},
                {field: 'action', caption: 'Действие',    size: 30, voc: data.vc_actions},
                {field: 'vc_users.label', caption: 'Оператор',    size: 30},

                {field: 'fiashouseguid', caption: 'Адрес', size: 50},
                {field: 'registrationtype', caption: 'Тип регистрации', size: 25, voc: data.vc_addr_reg_types},
                {field: 'apartmentnumber', caption: 'Номер квартиры', size: 20},
                {field: 'flatnumber', caption: 'Номер комнаты', size: 20},

                {field: 'soap.ts', caption: 'Отправлено',    size: 30, render: _ts, attr: $_USER.role.admin ? 'data-ref=1' : null},
                {field: 'soap.ts_rp', caption: 'Обработано',    size: 30, render: _ts, attr: $_USER.role.admin ? 'data-ref=1' : null},
                {field: 'soap.err_text', caption: 'Ошибка',    size: 30},
            ],
            
            url: '/_back/?type=citizen_compensations&part=log&id=' + $_REQUEST.id,
            
            onClick: function (e) {
            
                var c = this.columns [e.column]
                var r = this.get (e.recid)
                
                if ($_USER.role.admin) switch (c.field) {
                    case 'soap.ts':    if (r.uuid_out_soap) return openTab ('/out_soap_rq/' + r.uuid_out_soap)
                    case 'soap.ts_rp': if (r.uuid_message) return openTab ('/out_soap_rp/' + r.uuid_message)
                }
            
            }

        }).refresh ();

    }

})