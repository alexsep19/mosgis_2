define ([], function () {

    var grid_name = 'citizen_compensations_grid'

    return function (data, view) {

        var postData = {data: {}}

        if (!$_USER.role.admin)
            postData.data.uuid_org = $_USER.uuid_org


        data._can = {
            create: $_USER.has_nsi_20(7, 8)
        }

        $(w2ui ['rosters_layout'].el ('main')).w2regrid ({

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
//                    {
//                        type: 'button',
//                        id: 'importButton',
//                        caption: 'Импорт из ГИС ЖКХ',
//                        onClick: $_DO.import_citizen_compensations,
//                        icon: 'w2ui-icon-plus',
//                        off: !data._can.create
//                    },
                ].filter (not_off),
            },

            textSearch: 'contains',

            searches: [
                {field: 'person.label', caption: 'Ф.И.О', type: 'text'},
                {field: 'person.snils', caption: 'СНИЛС', type: 'text'},
                {field: 'vc_building_address.label', caption: 'Адрес', type: 'text'},
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
                {field: 'person.label', caption: 'Ф.И.О.', size: 100},
                {field: 'person.snils', caption: 'СНИЛС', size: 100},
                {field: 'addr', caption: 'Адрес', size: 30},
                {field: 'org.label', caption: 'Организация', size: 30},
//                {field: 'id_ctr_status', caption: 'Статус', size: 20, voc: data.vc_gis_status},

            ].filter (not_off),

            postData: postData,

            url: '/_back/?type=citizen_compensations',

            onDblClick: function (e) { openTab ('/citizen_compensation/' + e.recid) },

            onAdd: $_DO.create_citizen_compensations,

        })

    }

})