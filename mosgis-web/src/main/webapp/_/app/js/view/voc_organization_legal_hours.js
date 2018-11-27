define ([], function () {

    var grid_name = 'voc_organization_legal_hours_grid'

    return function (data, view) {

        data._can = {
            edit: $_USER.role.admin || $_USER.uuid_org == data.id
        }

        $_F5 = function (data) {

            var grid = w2ui [grid_name]

            grid.records = data.voc_organization_hours

            grid.refresh ()
        }

        var layout = w2ui ['voc_organization_legal_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2regrid ({

            name: grid_name,

            show: {
                toolbar: false,
                footer: true,
                toolbarReload: false,
                toolbarColumns: false,
            },

            textSearch: 'contains',

            columnGroups: [
                {caption: '', span: 1, master: true},
                {caption: 'Режим работы', span: 2},
                {caption: 'Перерыв', span: 2},
                {caption: 'Часы приема', span: 2},
                {caption: '', span: 1, master: true},
                {caption: '', span: 1, master: true}
            ],

            columns: [
                {field: 'label', caption: 'День недели', size: 60},
                {field: 'open_from', caption: 'с', size: 20
                    , editable: !data._can.edit? null : { type: 'time'}
                },
                {field: 'open_to', caption: 'по', size: 20
                    , editable: !data._can.edit ? null : {type: 'time'}
                },
                {field: 'break_from', caption: 'с', size: 20
                    , editable: !data._can.edit ? null : {type: 'time'}
                },
                {field: 'break_to', caption: 'по', size: 20
                    , editable: !data._can.edit ? null : {type: 'time'}
                },
                {field: 'reception_from', caption: 'с', size: 20
                    , editable: !data._can.edit ? null : {type: 'time'}
                },
                {field: 'reception_to', caption: 'по', size: 20
                    , editable: !data._can.edit ? null : {type: 'time'}
                },
                {field: 'is_holiday', caption: 'Выходной', size: 40
                    , editable: !data._can.edit ? null : {type: 'checkbox'}
                    , render: data._can.edit? null : function (v) {
                        return '<div style="text-align:center">' + (v ? 'Да' : 'Нет') + '</div>'
                    }
                },
                {field: 'note', caption: 'Комментарии', size: 300
                    , editable: !data._can.edit ? null : {type: 'text'}
                }
            ],

            records: [],

            onDblClick: null,

            onEditField: function (e) {

                var grid = this
                var record = grid.get(e.recid)
                var col = grid.columns [e.column]

                if (record.is_holiday == 1 && /(_to|_from)$/.test(col.field)) {
                    w2alert(record.label + ' выходной')
                    return e.preventDefault()
                }
                var editable = col.editable
                var v = record [col.field]

                if (editable.type == 'date') {
                    e.value = v ? new Date(v.substr(0, 10)) : new Date()
                } else {
                    e.value = v
                }
            },

            onChange: $_DO.patch_voc_organization_legal_hours,
        })

        $_F5 (data)

    }

})