define ([], function () {

    var grid_name = 'premise_usage_tarifs_grid'

    return function (data, view) {

        var it = data.item

        var postData = {data: {}}

        if (!$_USER.role.admin)
            postData.data.uuid_org = $_USER.uuid_org

        $(w2ui ['tarifs_layout'].el ('main')).w2regrid ({

            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: true,
                toolbarAdd: data._can.create,
                footer: 1,
                toolbarSearch: true
            },

            toolbar: {

                items: [
                ].filter (not_off),
            },

            textSearch: 'contains',

            searches: [
                {field: 'name', caption: 'Наименование', type: 'text'},
                {field: 'datefrom', caption: 'Дата начала действия', type: 'date'},
                {field: 'dateto', caption: 'Дата окончания действия', type: 'date'},
                {field: 'id_ctr_status', caption: 'Статус', type: 'enum'
                    , options: {items: data.vc_gis_status.items.filter(function (i) {
                        switch (i.id) {
                            case 10:
                            case 11:
                            case 12:
                            case 14:
                            case 34:
                            case 40:
                            case 102:
                            case 110:
                            case 104:
                                return true;
                            default:
                                return false;
                        }
                    })}
                },
            ].filter (not_off),

            columns: [

                {field: 'name', caption: 'Наименование', size: 100},
                {field: 'oktmo', caption: 'Территория действия', size: 30, render: function(i){
                        return i.oktmos.map((o) => o['vc_oktmo.code']).join('; ')
                }},
                {field: 'datefrom', caption: 'Дата начала действия', size: 30, render: _dt},
                {field: 'dateto', caption: 'Дата окончания действия', size: 30, render: _dt},
                {field: 'price', caption: 'Величина', size: 20},
                {field: 'org.label', caption: 'Организация', size: 30},

            ].filter (not_off),

            postData: postData,

            url: '/mosgis/_rest/?type=premise_usage_tarifs',

            onDblClick: function (e) {
                openTab ('/premise_usage_tarif/' + e.recid)
            },

            onAdd: $_DO.create_premise_usage_tarifs,

            onLoad: function (e) {

                 if (e.xhr.status != 200) return $_DO.apologize ({jqXHR: e.xhr})

                 var content = JSON.parse (e.xhr.responseText).content

                 var data = {
                     status : "success",
                     total  : content.cnt
                 }

                var uuid2oktmos = {}

                $.each (content.oktmos, function () {
                    uuid2oktmos[this.uuid] = uuid2oktmos[this.uuid] || []
                    uuid2oktmos[this.uuid].push(this)
                })

                var rs = dia2w2uiRecords (content.root)

                $.each (rs, function () {
                    this.oktmos = uuid2oktmos[this.uuid] || []
                })

                data.records = rs

                e.xhr.responseText = JSON.stringify (data)

             }
        })

    }

})