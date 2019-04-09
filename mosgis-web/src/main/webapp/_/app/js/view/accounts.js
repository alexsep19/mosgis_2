define ([], function () {

    var grid_name = 'accounts_grid'

    return function (data, view) {

        var is_popup = 1 == $_SESSION.delete('accounts_popup.on')

        var layout = w2ui ['popup_layout'] || w2ui ['service_payments_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2regrid ({

            multiSelect: false,

            name: grid_name + (is_popup? '_popup' : ''),

            show: {
                toolbar: true,
                footer: 1,
                toolbarReload: true,
                toolbarColumns: false,
                toolbarInput: false,
            },

            searches: [
            ].filter(not_off),

            textSearch: 'contains',

            columnGroups : [
                {master: true},
                {span: 3, caption: 'Площадь, м\xB2'},
                {master: true},
                {span: 4, caption: 'Плательщик'},
                {master: true},
            ],

            columns: [
                {field: 'accountnumber', caption: 'Номер', size: 20},

                {field: 'totalsquare', caption: 'Общая', size: 20, render: 'float:2'},
                {field: 'residentialsquare', caption: 'Жилая', size: 20, render: 'float:2'},
                {field: 'heatedarea', caption: 'Отапливаемая', size: 20, render: 'float:2'},

                {field: 'livingpersonsnumber', caption: 'К-во прож.', size: 20, render: 'int'},

                {field: 'ind.label', caption: 'Физ. лицо', size: 50},
                {field: 'org.label', caption: 'Юр. лицо', size: 50},
                {field: 'isrenter',  caption: 'Нанинматель?', size: 10, voc: {0: 'нет', 1: 'да'}},
                {field: 'isaccountsdivided', caption: 'Разделён?', size: 10, voc: {0: 'нет', 1: 'да'}},
                {field: 'id_ctr_status', caption: 'Статус', size: 10, voc: data.vc_gis_status},
            ],

            onRequest: function (e) {

                if (is_popup) {

                    var post_data = $('body').data('accounts_popup.post_data')

                    if (post_data) {

                        if (e.postData.search) {
                            $.each(e.postData.search, function () {
                                post_data.search.push(this)
                            })
                        }

                        $.extend(e.postData, post_data)
                    }
                }
            },

            postData: {data: {uuid_org: $_USER.uuid_org}},

            url: '/_back/?type=accounts',

            onDblClick: function (e) {

                var r = this.get (e.recid)

                if (is_popup) {

                    $_SESSION.set ('accounts_popup.data', clone (r))

                    w2popup.close ()

                }
                else {

                    openTab ('/account/' + e.recid)

                }

            },
        })

    }

})