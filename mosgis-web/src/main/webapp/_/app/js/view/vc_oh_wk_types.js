define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['vocs_layout'].el ('main')).w2regrid ({ 

            name: 'vc_oh_wk_types_grid',

            toolbar: {
            
                items: [
                
                    {
                        type: 'button', 
                        id: 'import', 
                        caption: 'Импорт справочника из ГИС ЖКХ', 
                        icon: 'w2ui-icon-plus', 
                        onClick: $_DO.import_vc_oh_wk_types,
                        off: !$_USER.role.nsi_20_7
                    }
                    
                ].filter (not_off),
                
            },

            show: {
                toolbar: true,
                toolbarInput: false,
                toolbarAdd: $_USER.role.nsi_20_7,
                footer: true,
            },

            columns: [
                {field: 'code', caption: 'Код', size: 10},
                {field: 'servicename', caption: 'Наименование', size: 10},
                {field: 'code_vc_nsi_218', caption: 'Группа работ', size: 10, voc: data.vc_nsi_218},
                {field: 'id_owt_status', caption: 'Статус', size: 10, voc: data.vc_gis_status},
                {field: 'err_text', caption: 'Ошибка', size: 10}
            ],
            
            url: '/mosgis/_rest/?type=voc_overhaul_work_types',

            onAdd: $_DO.create_vc_oh_wk_types,
            
            onDblClick: function (e) {
                openTab ('/vc_oh_wk_type/' + e.recid)
            },

        }).refresh ();

    }

})