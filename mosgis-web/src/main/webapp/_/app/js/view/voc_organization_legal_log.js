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

        $(w2ui ['topmost_layout'].el ('main')).w2regrid ({ 

            name: 'voc_organization_legal_log',

            show: {
                toolbar: true,
                toolbarInput: false,
                footer: true,
            },     

            columnGroups : [
                {span: 3, caption: 'Действие'},
                {span: 8 + 24, caption: 'Значения полей'},
                {span: 2, caption: 'Запрос в ГИС ЖКХ'},
            ], 

            columns: [                
                {field: 'ts', caption: 'Дата/время',    size: 30, render: _ts},
                {field: 'vc_users.label', caption: 'Оператор',    size: 30},
                {field: 'action', caption: 'Действие',    size: 30, voc: data.vc_actions},

                {field: 'fullname', caption: 'Полное наименование', size: 100},
                {field: 'address', caption: 'Адрес', size: 10},
                {field: 'ogrn', caption: 'ОГРН', size: 15},
                {field: 'inn', caption: 'ИНН', size: 15},
                {field: 'kpp', caption: 'КПП', size: 10},
                {field: 'okopf', caption: 'ОКОПФ', size: 5},
                {field: 'stateregistrationdate', caption: 'Дата государственной регистрации', size: 18, render: _dt},
                {field: 'activityenddate', caption: 'Дата прекращения деятельности', size: 18, render: _dt},

                {field: "phone", caption: "Телефон организации", size: 10, hidden: true},
                {field: "mail", caption: "Электронный адрес", size: 15, hidden: true},
                {field: "site", caption: "Адрес официального сайта", size: 15, hidden: true},
                {field: "phone_support", caption: "Номер телефона горячей линии", size: 10, hidden: true},

                {field: "head_fio", caption: "ФИО руководителя", size: 20, hidden: true},
                {field: "head_post", caption: "Должность руководителя", size: 20, hidden: true},
                {field: "head_phone", caption: "Телефон руководителя", size: 10, hidden: true},
                {field: "head_mail", caption: "Электронный адрес руководителя", size: 15, hidden: true},

                {field: "vice_fio", caption: "ФИО заместителя", size: 20, hidden: true},
                {field: "vice_post", caption: "Должность заместителя", size: 20, hidden: true},
                {field: "vice_phone", caption: "Телефон заместителя", size: 10, hidden: true},
                {field: "vice_mail", caption: "Электронный адрес заместителя", size: 20, hidden: true},

                {field: "citizen_address", caption: "Адрес приема граждан", size: 20, hidden: true},
                {field: "citizen_phone", caption: "Телефон приема граждан", size: 10, hidden: true},
                {field: "citizen_place", caption: "Место размещения информации для граждан", size: 20, hidden: true},

                {field: "dispatch_address", caption: "Адрес диспетчерской службы", size: 20, hidden: true},
                {field: "dispatch_phone", caption: "Контактные телефоны", size: 20, hidden: true},
                {field: "dispatch_schedule", caption: "Режим работы", size: 20, hidden: true},
                {
                    field: "slf_mng_org",
                    caption: "Наименование саморегулируемой организации",
                    off: !data.is_on_self_manage,
                    size: 10,
                    hidden: true
                },

                {field: "dt_from_slf_mng_org", caption: "Дата вступления в члены организации", off: !data.is_on_self_manage, size: 15, render: _dt, hidden: true},
                {field: "dt_to_slf_mng_org", caption: "Дата исключения/выхода из членов организации", off: !data.is_on_self_manage, size: 15, render: _dt, hidden: true},
                {field: "rsn_slf_mng_org", caption: "Причина исключения из членов организации", off: !data.is_on_self_manage, size: 20, hidden: true},

                {field: "staff_cnt", caption: "Количество штатных единиц", size: 4, hidden: true},
                {field: "staff_work_cnt", caption: "Количество работающих человек", size: 4, hidden: true},

                {field: 'soap.ts', caption: 'Отправлено',    size: 30, render: _ts, attr: 'data-ref=1'},
                {field: 'soap.ts_rp', caption: 'Обработано',    size: 30, render: _ts, attr: 'data-ref=1'},
            ],

            url: '/mosgis/_rest/?type=voc_organizations&part=log&id=' + $_REQUEST.id,            
            
            onClick: function (e) {
            
                var c = this.columns [e.column]
                var r = this.get (e.recid)
                
                switch (c.field) {
                    case 'soap.ts':    if (r.uuid_out_soap) return openTab ('/out_soap_rq/' + r.uuid_out_soap)
                    case 'soap.ts_rp': if (r ['soap.uuid_ack']) return openTab ('/out_soap_rp/' + r ['soap.uuid_ack'])
                }
            
            },
            
        }).refresh ();

        w2ui['voc_organization_legal_log'].on('columnOnOff:after', function () {
            this.resize()
        })
    }

})