define ([], function () {

    var grid_name = 'tarif_diffs_grid'

    return function (data, view) {

        var it = data.item

        var postData = {data: {uuid_tf: $_REQUEST.id}}

        if (!$_USER.role.admin)
            postData.data.uuid_org = $_USER.uuid_org

        $(w2ui ['topmost_layout'].el ('main')).w2regrid ({

            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: true,
                toolbarAdd: false,
                toolbarDelete: data._can.delete,
                toolbarEdit: data._can.edit,
                footer: 1,
                toolbarSearch: false,
                toolbarInput: false,
                toolbarColumns: true
            },

            toolbar: {
                onClick: $_DO.create_tarif_diffs,
                items: [
                    {
                        id: 'create',
                        type: 'menu',
                        text: 'Добавить',
                        selected: -1,
                        items: data.vc_diff_value_types.items.map((i) => {
                            return {
                                id: i.id,
                                text: i.text
                            }
                        }),
                        off: !data._can.create
                    },
                ].filter (not_off),
            },

            textSearch: 'contains',

            searches: [
            ].filter (not_off),

            columns: [

                {field: 'vc_diff.differentiationname', caption: 'Критерий дифференциации', size: 100, render: function (i) {
                    if (i.id_type == 'Multiline') {
                        return i['vc_diff.differentiationname'] + ': ' + i['label']
                    }
                    return i['vc_diff.differentiationname']
                }},
                {field: 'value', caption: 'Значение критерия дифференциации', size: 30, render: function(i) {
                    var field = 'value' + i.id_type.toLowerCase()
                    if (i.id_type == 'Multiline') {
                        field = 'valuestring'
                    }
                    var value = i [field];
                    var value_to = i [field + '_to'];

                    switch(i.id_type) {
                        case 'Date':
                            value = dt_dmy(value)
                            value_to = dt_dmy(value_to)
                            break;
                        case 'Boolean':
                            value = value? 'Да' : 'Нет'
                            break;
                        case 'FIAS':
                            value = i.fias.map((f) => f.text).join('; ')
                            break;
                        case 'OKTMO':
                            value = i.oktmo.map((f) => f.text).join('; ')
                            break;
                        case 'Enumeration':
                            value = i.enumeration.map((f) => f.text).join('; ')
                            switch(i['op.id']) {
                                case 'Range': return 'включая значения: ' + value
                                case 'ExcludingRange': return 'исключая значения: ' + value
                            }
                            break;
                    }

                    if (!i['op.id']) {
                        return value
                    }
                    if (!/Range/.test(i['op.id'])) {
                        return i.op.label + ' ' + value
                    }

                    value = 'с ' + value + (value_to? (' по ' + value_to) : '')

                    switch (i['op.id']) {
                        case 'Range'         : return 'в диапазоне ' + value
                        case 'ExcludingRange': return 'исключая диапазон ' + value
                        default: return ''
                    }
                }},

            ].filter (not_off),

            postData: postData,

            url: '/_back/?type=tarif_diffs',

            onLoad: function (e) {

                if (e.xhr.status != 200) return $_DO.apologize ({jqXHR: e.xhr})

                var content = JSON.parse (e.xhr.responseText).content

                var data = {
                    status : "success",
                    total  : content.cnt
                }

                var rs = dia2w2uiRecords (content.root)

                fix_records(rs, content)

                data.records = rs

                e.xhr.responseText = JSON.stringify (data)
            },

            onDblClick: data._can.edit? $_DO.edit_tarif_diffs : null,

            onAdd: $_DO.create_tarif_diffs,

            onEdit: $_DO.edit_tarif_diffs,

            onDelete: $_DO.delete_tarif_diffs
        })

        function fix_records(rs, content){

            var idx = {}

            $.each(rs, function () {
                this.idx_fias  = {}
                this.idx_oktmo = {}
                this.idx_enumeration = {}
                idx[this.uuid] = this
            })

            $.each(content.fias, function () {
                idx[this.uuid].idx_fias[this.fiashouseguid] = {
                    id: this.fiashouseguid,
                    text: this['b.postalcode'] + ' ' + this['b.label']
                }
            })

            $.each(content.oktmo, function () {
                idx[this.uuid].idx_oktmo[this.oktmo] = {
                    id: this.oktmo,
                    code: this['o.code'],
                    text: this['o.code'] + ' ' + this['o.site_name']
                }
            })

            $.each(content.enumeration, function () {
                idx[this.uuid].idx_enumeration[this.id] = {
                    id: this.id,
                    text: this.label
                }
            })

            $.each(rs, function () {
                switch (this.id_type) {
                    case 'FIAS':
                        this.fias = Object.values(this.idx_fias) || []
                        break;
                    case 'OKTMO':
                        this.oktmo = Object.values(this.idx_oktmo) || []
                        break;
                    case 'Enumeration':
                        this.enumeration = Object.values(this.idx_enumeration) || []
                        this.registrynumber = this['vc_diff.nsiitem']
                }
            })
        }
    }

})