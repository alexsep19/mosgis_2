define ([], function () {

    return function (data, view) {

        data._can = {
            add_branch: !$_USER.role.admin,
        }
        
        data._can.add_alien = data._can.add_branch
        
        var is_popup = 1 == $_SESSION.delete ('voc_organization_proposals_popup.on')

        data.label = 'Создание обособленных подразделений и ФПИЮЛ'

        $('title').text(data.label)

        fill(view, data, $('#body'))

        $('#container').w2regrid ({

            name: 'voc_organization_proposals_grid' + (is_popup ? '_popup' : ''),

            show: {
                toolbar: true,
                toolbarColumns: false,
                footer: true
            },

            searches: [
                {field: 'ogrn',      caption: 'ОГРН(ИП)',            type: 'text', operator: 'is', operators: ['is']},
                {field: 'label_uc',  caption: 'Наименование',  type: 'text'},
                {field: 'inn',       caption: 'ИНН',                 type: 'text', operator: 'is', operators: ['is']},
                {field: 'kpp',       caption: 'КПП',                 type: 'text', operator: 'is', operators: ['is']},
                {field: 'id_type', caption: 'Тип',     type: 'enum'
                    , hidden: is_popup
                    , options: {
                        items: data.vc_organization_types.items.map(function(i) {
                            if (i.id == 3) { // HACK: длинный ФПИЮЛ (...) коверкает окно поиска
                                i.text = i.text.replace(/ \(.*\)/, '')
                            }
                            return i
                        })
                    }
                },
            ],

            columns: [
                {field: 'ogrn', caption: 'ОГРН(ИП)',    size: 20},
                {field: 'label', caption: 'Наименование',    size: 100},
                {field: 'inn', caption: 'ИНН',    size: 15},
                {field: 'kpp', caption: 'КПП',    size: 10},
            ],

            url: '/_back/?type=voc_organization_proposals',

            toolbar: {
                items: [
                    {
                        type: 'button',
                        id: 'add_branch',
                        caption: 'Добавить обособленное подразделение',
                        off: !data._can.add_branch
                    },
                    {
                        type: 'button',
                        id: 'add_alien',
                        caption: 'Добавить ФПИЮЛ',
                        off: !data._can.add_alien
                    }
                ].filter(not_off),
                onClick: function (target, e) {
                    var handler = $_DO[target + '_voc_organization_proposals']

                    if (!handler) {
                        return false
                    }

                    handler(e)
                }
            },

            onMenuClick: function (e) {
                var handler = $_DO [e.menuItem.id + '_voc_organization_proposals']
                var grid = this
                var record = this.get(e.recid)

                if (!handler) {
                    throw 'voc_organization_proposals.not_implemented'
                }

                var confirm = e.menuItem.confirm;
                var preconfirm = e.menuItem.preconfirm;

                if (!confirm) {
                    handler(record, grid)
                    return
                }
                if (preconfirm && !preconfirm(record)) {
                    return
                }

                w2confirm(confirm).yes(function (answer) {
                    handler(record, data, grid)
                })
            },

            onDblClick: function (e) {

                var r = this.get (e.recid)

                if (is_popup) {

                    $_SESSION.set ('voc_organization_proposals_popup.data', clone (r))

                    w2popup.close ()

                }
                else {

                    function show (postfix) {openTab ('/voc_organization_proposal_' + postfix + '/' + r.uuid)}

                    switch (r.id_type) {
                        case 2: return show ('branch')
                        case 3: return show ('alien')
                    }

                }

            },

            onRefresh: function (e) {

                e.done (function () {

                    if (this.searchData.length > 0 && this.records.length == 0) {

                        if ($_SESSION.delete ('importing')) {
                            w2alert ('Запрос в ГИС ЖКХ не дал результатов. Вероятно, Вы опечатались, вводя ОГРН[ИП]')
                        }

                    }

                    $('#tb_voc_organization_proposals_grid_toolbar_item_w2ui-search .w2ui-search-all').attr ({
                        style: 'width: 450px !important',
                        placeholder: 'Введите полный ОГРН[ИП], ИНН или подстроку наименования',
                        title: 'Указав ИНН или ОГРН, Вы можете добавить через пробел КПП.\n\nДостаточно первых двух цифр, например: 7711122233 77',
                    })

                    $('#tb_voc_organization_proposals_grid_toolbar_item_w2ui-search .w2ui-toolbar-search').attr ({
                        style: 'width: 450px !important',
                    })

                })

            },

        }).refresh ();

    }

})