define ([], function () {

    return function (data, view) {
        
        $(w2ui ['rosters_layout'].el ('main')).w2regrid ({ 

            name: 'infrastructures_grid',             

            show: {
                toolbar: true,
                footer: true,
            },     
            
            searches: [
                {field: 'id_is_status', caption: 'Статус', type: 'enum', options: {items: data.vc_gis_status.items}},
                {field: 'code_vc_nsi_33', caption: 'Вид объекта', type: 'enum', options: {items: data.vc_nsi_33.items}},
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
            }

        }).refresh ();

    }

})