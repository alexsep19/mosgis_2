define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['integration_layout'].el ('main')).w2regrid ({ 

            name: 'out_soap_grid',

            show: {
                toolbar: true,
                toolbarSearch: true,
                toolbarInput: false,
                footer: true,
            },            
            
            columnGroups : [
                {span: 4, caption: 'Запрос'},
                {span: 3, caption: 'Ответ'},
            ], 

            columns: [                
                {field: 'ts', caption: 'Запрос',    size: 25, attr: 'data-ref=1'},
                {field: 'svc', caption: 'Сервис',    size: 30},
                {field: 'op', caption: 'Метод',    size: 30},
                {field: 'org.label', caption: 'Организация',    size: 30},
                
                {field: 'ts_rp', caption: 'Ответ',    size: 25, attr: 'data-ref=1'},
                {field: 'err_code', caption: 'Код ошибки',    size: 10},
                {field: 'err_text', caption: 'Текст ошибки',    size: 50},
            ],

            url: '/_back/?type=out_soap',
            
            limit: 50,
            
            onDblClick: function (e) {},
            
            onClick: function (e) {
            
                var c = this.columns [e.column]
                var r = this.get (e.recid)
                
                switch (c.field) {
                    case 'ts':    if (r.uuid) return openTab ('/out_soap_rq/' + r.uuid)
                        break
                    case 'ts_rp': if (r.uuid_ack) return openTab ('/out_soap_rp/' + r.uuid_ack)
                        break
                }
                
            },

        }).refresh ();

    }

})