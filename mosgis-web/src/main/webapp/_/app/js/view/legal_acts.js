define ([], function () {

    var grid_name = 'legal_acts_grid'

    return function (data, view) {

        var it = data.item

        var postData = {data: {}}

        if (!$_USER.role.admin)
            postData.data.uuid_org = $_USER.uuid_org

        var is_popup = 1 == $_SESSION.delete('legal_acts_popup.on')

        $((w2ui ['popup_layout'] || w2ui ['rosters_layout']).el ('main')).w2regrid ({

            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: true,
                toolbarAdd: data._can.create && !is_popup,
                footer: 1,
                toolbarSearch: true
            },

            toolbar: {

                items: [
                    {
                        type: 'button',
                        id: 'importButton',
                        caption: 'Импорт из ГИС ЖКХ',
                        onClick: $_DO.import_legal_acts,
                        icon: 'w2ui-icon-plus',
                        off: !data._can.create || is_popup
                    },
                ].filter (not_off),
            },

            textSearch: 'contains',

            searches: [
                {field: 'docnumber', caption: 'Номер документа', type: 'text'},
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

                {field: 'id_ctr_status', caption: 'Статус', size: 20, voc: data.vc_gis_status},
                {field: 'level_', caption: 'Уровень', size: 20, voc: data.vc_legal_act_levels},
                {field: 'code_vc_nsi_324', caption: 'Вид документа', size: 30, voc: data.vc_nsi_324},
                {field: 'name', caption: 'Наименование документа', size: 100},
                {field: 'docnumber', caption: 'Номер документа', size: 30},
                {field: 'approvedate', caption: 'Дата принятия', size: 30, render: _dt},
                {field: 'org.label', caption: 'Организация', size: 30},

            ].filter (not_off),

            postData: postData,

            url: '/_back/?type=legal_acts',

            onDblClick: function (e) {

                var r = this.get (e.recid)

                if (is_popup) {

                    $_SESSION.set ('legal_acts_popup.data', clone (r))

                    w2popup.close ()

                }
                else {
                    openTab ('/legal_act/' + e.recid)
                }
            },

            onAdd: $_DO.create_legal_acts,

        })

    }

})