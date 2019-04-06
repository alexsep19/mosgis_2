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
                {master: true},
                {span: 3, caption: 'Площадь, м\xB2'},
                {master: true},
                {span: 4, caption: 'Плательщик'},
                {span: 3, caption: 'Запрос в ГИС ЖКХ'},
            ], 
            
            columns: [                
                {field: 'ts', caption: 'Дата/время',    size: 30, render: _ts},
                {field: 'action', caption: 'Действие',    size: 30, voc: data.vc_actions},
                {field: 'vc_users.label', caption: 'Оператор',    size: 30},

                {field: 'accountnumber', caption: 'Номер', size: 20},
                
                {field: 'totalsquare', caption: 'Общая', size: 20, render: 'float:2'},
                {field: 'residentialsquare', caption: 'Жилая', size: 20, render: 'float:2'},
                {field: 'heatedarea', caption: 'Отапливаемая', size: 20, render: 'float:2'},

                {field: 'livingpersonsnumber', caption: 'К-во прож.', size: 20, render: 'int'},
                
                {field: 'ind.label', caption: 'Физ. лицо', size: 50},
                {field: 'org.label', caption: 'Юр. лицо', size: 50},
                {field: 'isrenter',  caption: 'Нанинматель?', size: 10, voc: {0: 'нет', 1: 'да'}},
                {field: 'isaccountsdivided', caption: 'Разделён?', size: 10, voc: {0: 'нет', 1: 'да'}},
                
                {field: 'soap.ts', caption: 'Отправлено',    size: 30, render: _ts, attr: 'data-ref=1'},
                {field: 'soap.ts_rp', caption: 'Обработано',    size: 30, render: _ts, attr: 'data-ref=1'},
                {field: 'soap.err_text', caption: 'Ошибка',    size: 30},

            ],
            
            url: '/_back/?type=accounts&part=log&id=' + $_REQUEST.id,            
            
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