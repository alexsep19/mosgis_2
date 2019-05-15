define ([], function () {

    return function (data, view) {

        var is_popup = 1 == $_SESSION.delete ('voc_organizations_popup.on')

        data = $('body').data ('data')

        $((w2ui ['popup_layout'] || w2ui ['rosters_layout']).el ('main')).w2regrid ({

            name: 'voc_organizations_grid' + (is_popup ? '_popup' : ''),

            show: {
                toolbarAdd: !$_USER.role.admin,
                toolbar: true,
                footer: true
            },
            
            toolbar: {

                items: [
                    {type: 'button', caption: 'Массовый импорт из ГИС ЖКХ', onClick: $_DO.import_voc_organizations, icon: 'w2ui-icon-plus', off: !$_USER.role.admin},
                ].filter (not_off),

            },

            searches: [
                {field: 'ogrn',      caption: 'ОГРН(ИП)',            type: 'text', operator: 'is', operators: ['is']},
                {field: 'label_uc',  caption: 'Наименование / ФИО',  type: 'text'},
                {field: 'inn',       caption: 'ИНН',                 type: 'text', operator: 'is', operators: ['is']},
                {field: 'kpp',       caption: 'КПП',                 type: 'text', operator: 'is', operators: ['is']},
                {field: 'code_vc_nsi_20', caption: 'Полномочия',     type: 'enum', options: {items: data.vc_nsi_20.items}},
                {field: 'id_type', caption: 'Типы',     type: 'enum', options: {items: data.vc_organization_types.items}
//                    , hidden: is_popup
                },
            ],

            columns: [
                {field: 'ogrn', caption: 'ОГРН(ИП)',    size: 20},
                {field: 'label', caption: 'Наименование (ФИО)',    size: 100},
                {field: 'inn', caption: 'ИНН',    size: 15},
                {field: 'kpp', caption: 'КПП',    size: 10},
                {field: 'id_dlg_status', caption: 'Делегирование прав', size: 10, voc: data.vc_delegation_status},
            ],

            url: '/_back/?type=voc_organizations',

            onRequest: function (e) {

                if (is_popup) {

                    var id_type = $_SESSION.delete('voc_organization_popup.id_type');

                    if (id_type) {
                        e.postData.id_type = id_type
                    }


                    var post_data = this.post_data || $_SESSION.delete('voc_organization_popup.post_data');

                    if (post_data) {

                        if (e.postData.search) {
                            $.each(e.postData.search, function () {
                                post_data.search.push(this)
                            })
                        }

                        $.extend(e.postData, post_data)

                        this.post_data = post_data
                    }
                }
            },

            onAdd: function (e) {
                openTab ('/voc_organization_proposals')
            },

            onDblClick: function (e) {

                var r = this.get (e.recid)

                if (is_popup) {

                    $_SESSION.set ('voc_organizations_popup.data', clone (r))

                    w2popup.close ()

                }
                else {

                    function show (postfix) {openTab ('/voc_organization_' + postfix + '/' + r.uuid)}

                    switch (String (r.ogrn).length) {
                        case 13: return show ('legal')
                        case 15: return show ('individual')
                    }

                }

            },

            onRefresh: function (e) {

                e.done (function () {

                    if (this.searchData.length > 0 && this.records.length == 0) {

                        if ($_SESSION.delete ('importing')) {
                            w2alert ('Запрос в ГИС ЖКХ не дал результатов. Вероятно, Вы опечатались, вводя ОГРН[ИП]')
                        }
                        else if ($_SESSION.delete ('first_post')) {
                            $_DO.check_voc_organizations (e)
                        }
                        else {
                            $_SESSION.set ('first_post', 1)
                        }

                    }

                    $('#tb_voc_organizations_grid_toolbar_item_w2ui-search .w2ui-search-all').attr ({
                        style: 'width: 450px !important',
                        placeholder: 'Введите полный ОГРН[ИП], ИНН или подстроку наименования/ФИО для поиска',
                        title: 'Указав ИНН или ОГРН, Вы можете добавить через пробел КПП.\n\nДостаточно первых двух цифр, например: 7711122233 77',
                    })

                    $('#tb_voc_organizations_grid_toolbar_item_w2ui-search .w2ui-toolbar-search').attr ({
                        style: 'width: 450px !important',
                    })

                })

            },

        }).refresh ();

    }

})