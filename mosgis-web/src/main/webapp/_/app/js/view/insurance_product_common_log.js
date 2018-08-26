define ([], function () {

    return function (data, view) {

        data = $('body').data ('data')

        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: 'insurance_product_common_log',

            show: {
                toolbar: true,
                toolbarInput: false,
                footer: true,
            },     

            columnGroups : [
                {span: 3, caption: 'Событие'},
                {span: 5, caption: 'Значения полей'},
                {span: 4, caption: 'Запрос в ГИС ЖКХ'},
            ], 

            columns: [                
                {field: 'ts', caption: 'Дата/время',    size: 30, render: _ts},
                {field: 'action', caption: 'Действие',    size: 30, voc: {
                    create: 'Создание',
                    update: 'Редактирование',
                    delete: 'Удаление',
                    undelete: 'Восстановление',
                }},
                {field: 'vc_users.label', caption: 'Оператор',    size: 30},

                {field: 'description', caption: 'Описание', size: 50},
                {field: 'org_ins.label', caption: 'Страховщик', size: 100},
                {field: 'name', caption: 'Имя файла', size: 50, attr: 'data-ref=1'},
                {field: 'len', caption: 'Объём, Мб', size: 10, render: function (r) {return (r.len/1024/1024).toFixed(3)}},
                {field: 'is_deleted',  caption: 'Статус',     size: 20, voc: {0: 'Актуально', 1: 'Удалено'}},
                
                {field: 'soap.ts', caption: 'Отправлено',    size: 30, render: _ts, attr: 'data-ref=1'},
                {field: 'soap.ts_rp', caption: 'Обработано',    size: 30, render: _ts, attr: 'data-ref=1'},
                {field: 'soap.id_status', caption: 'Статус',    size: 30, render: function (r, i, c, v) {
                    switch (v) {
                        case 3: return 'Обработано'
                        case 2: return 'Ожидает ответа'
                        default: return 'Ожидает отправки'
                    }
                }},
                {field: 'soap.err_text', caption: 'Ошибка',    size: 30},

            ],

            postData: {data: {uuid_object: $_REQUEST.id}},

            url: '/mosgis/_rest/?type=insurance_product_log',

            onClick: function (e) {

                var c = this.columns [e.column]
                var r = this.get (e.recid)

                switch (c.field) {
                    case 'name':       return $_DO.download_insurance_product_common_log (e)
                    case 'soap.ts':    if (r.uuid_out_soap) return openTab ('/out_soap_rq/' + r.uuid_out_soap)
                    case 'soap.ts_rp': if (r.uuid_message) return openTab ('/out_soap_rp/' + r.uuid_message)
                }

            }

        }).refresh ();

    }

})