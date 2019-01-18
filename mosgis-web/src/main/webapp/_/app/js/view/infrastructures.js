define ([], function () {

    function perms () {

        return $_USER.role.admin || $_USER.has_nsi_20 (2, 8)

    }

    return function (data, view) {
        
        $(w2ui ['rosters_layout'].el ('main')).w2regrid ({

            name: 'infrastructures_grid',             

            show: {
                toolbar: true,
                toolbarInput: false,
                footer: true,
            },

            toolbar: {
                items: [
                    {type: 'button', id: 'createButton', caption: 'Добавить', onClick: $_DO.create_infrastructure, icon: 'w2ui-icon-plus', off: !perms ()},
                    {type: 'button', id: 'deleteButton', caption: 'Удалить', onClick: $_DO.delete_infrastructure, icon: 'w2ui-icon-cross', disabled: true, off: !perms ()},
                ].filter (not_off)
            },
            
            searches: [
                {field: 'id_is_status', caption: 'Статус', type: 'enum', options: {items: data.vc_gis_status.items}},
                {field: 'code_vc_nsi_33', caption: 'Вид объекта', type: 'enum', options: {items: data.vc_nsi_33.items}},
                {field: 'oktmo_code', caption: 'ОКТМО', type: 'text'},
                {field: 'manageroki_label', caption: 'Правообладатель', type: 'text'},
                {field: 'endmanagmentdate', caption: 'Окончание управления', type: 'date'},
                {field: 'is_deleted', caption: 'Статус записи', type: 'enum', options: {items: [
                    {id: "0", text: "Актуальные"},
                    {id: "1", text: "Удалённые"},
                ]}}
            ],

            columns: [
                {field: 'code_vc_nsi_33', caption: 'Вид объекта', size: 30, voc: data.vc_nsi_33},
                {field: 'name', caption: 'Наименование', size: 30},
                {field: 'adress', caption: 'Адрес', size: 30},
                {field: 'manageroki_label', caption: 'Правообладатель', size: 30},
                {filed: 'endmanagmentdate', caption: 'Окончание управления', size: 10},
                {field: 'id_is_status', caption: 'Статус', size: 15},
                {field: 'id_is_status_gis', caption: 'Статус в ГИС ЖКХ', size: 15},
            ],

            url: '/mosgis/_rest/?type=infrastructures',
            
            onDblClick: function (e) {
                openTab ('/infrastructure/' + e.recid)
            },

            onRefresh: function (e) {e.done (function () {

                if ($_USER.role.nsi_20_8) {

                    oktmos = Object.keys($_USER.role).filter ((x) => x.startsWith ('oktmo_')).map ((x) => { return {id: x.substring ('oktmo_'.length)}})

                    this.postData = {
                        search: {field: 'oktmo_code', operator: 'in', value: oktmos}
                    }

                }

            })}

        }).refresh ();

    }

})