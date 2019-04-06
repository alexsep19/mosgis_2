define ([], function () {

    return function (data, view) {

        data._can = {
            create: $_USER.role.admin || $_USER.uuid_org == $_REQUEST.id,
            delete: $_USER.role.admin || $_USER.uuid_org == $_REQUEST.id,
        }

        var is_popup = 1 == $_SESSION.delete ('voc_organization_legal_members_popup.on')

        var layout = w2ui ['voc_organization_legal_layout']

        var $panel = $(layout.el('main'))

        $panel.w2regrid({

            name: 'voc_organization_legal_members_grid',

            toolbar: {

                items: [
                    {type: 'button', id: 'create_person', caption: 'Физическое лицо', icon: 'w2ui-icon-plus'
                        , onClick: $_DO.create_person_voc_organization_legal_members
                        , off: !data._can.create
                    },
                    {type: 'button', id: 'create_org', caption: 'Юридическое лицо', icon: 'w2ui-icon-plus'
                        , onClick: $_DO.create_org_voc_organization_legal_members
                        , off: !data._can.create
                    }
                ].filter(not_off),
            },

            show: {
                toolbar: true,
                toolbarInput: false,
                toolbarDelete: data._can.delete,
                toolbarColumns: false,
                footer: true
            },

            searches: [
                {field: 'dt',         caption: 'На дату',       type: 'date', operator: 'is', operators: ['is'], value: new Date()},
                {field: 'is_deleted', caption: 'Статус записи', type: 'enum', options: {items: [
                    {id: "0", text: "Актуальные"},
                    {id: "1", text: "Удалённые"},
                ]}},
                {field: 'id_type',     caption: 'Тип лица',      type: 'list', operator: 'is', operators: ['is'], options: {items: [
                    {id: "0", text: "Юридическое лицо"},
                    {id: "1", text: "Физическое лицо"},
                ]}},
            ],

            columns: [
                {field: 'author_label', caption: 'Поставщик информации',    size: 50},
                {field: 'label', caption: 'ФИО/Наименование организации',    size: 50},
                {field: 'participants.label', caption: 'Участие в совете правления, ревизионной комиссии',    size: 50},
                {field: 'dt_from', caption: 'Дата принятия',    size: 16},
                {field: 'dt_to', caption: 'Дата исключения', size: 16},
            ],

            url: '/_back/?type=organization_members',

            postData: {data: {"uuid_org": $_REQUEST.id}},

            onDblClick: function (e) {

                var r = this.get (e.recid)

                if (is_popup) {

                    $_SESSION.set ('voc_organization_legal_members_popup.data', clone (r))

                    w2popup.close ()

                }
                else {

                    openTab ('/organization_member/' + r.id)
                }

            },

            onDelete: $_DO.delete_voc_organization_legal_members

        }).refresh ();

    }

})