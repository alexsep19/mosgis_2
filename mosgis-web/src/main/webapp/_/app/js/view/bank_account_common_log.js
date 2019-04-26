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

            name: 'bank_account_common_log',

            show: {
                toolbar: true,
                toolbarInput: false,
                footer: true,
            },

            columnGroups : [
                {span: 4, caption: 'Событие'},
                {span: 4, caption: 'Значения полей'},
//                {span: 3, caption: 'Запрос в ГИС ЖКХ'},
            ],

            columns: [
                {field: 'ts', caption: 'Дата/время',    size: 30, render: _ts},
                {field: 'vc_users.label', caption: 'Оператор',    size: 30},
                {field: 'action', caption: 'Действие',    size: 30, voc: data.vc_actions},
                {field: 'id_ctr_status',  caption: 'Статус',     size: 10, voc: data.vc_gis_status},

                {field: 'accountnumber', caption: '№ расчетного счёта', size: 20},
                {field: 'bank_label', caption: 'БИК / банк', size: 30},
                {field: 'opendate', caption: 'Дата открытия', size: 18, render: _dt},
                {field: 'closedate', caption: 'Дата закрытия', size: 18, render: _dt},

//                {field: 'soap.ts', caption: 'Отправлено',    size: 30, render: _ts, attr: $_USER.role.admin ? 'data-ref=1' : null},
//                {field: 'soap.ts_rp', caption: 'Обработано',    size: 30, render: _ts, attr: $_USER.role.admin ? 'data-ref=1' : null},
//                {field: 'soap.err_text', caption: 'Ошибка',    size: 30},
            ],

            url: '/_back/?type=bank_accounts&part=log&id=' + $_REQUEST.id,

            onClick: function (e) {

                var c = this.columns [e.column]
                var r = this.get (e.recid)

                if ($_USER.role.admin) switch (c.field) {
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

                    $.each (rs, function () {
                        this['bank_label'] = this['bikcredorg.label']
                    })

                    data.records = rs

                    e.xhr.responseText = JSON.stringify (data)

                }

            }
        }).refresh ();

    }

})