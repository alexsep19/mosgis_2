define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['vocs_layout'].el ('main')).w2regrid ({ 

            name: 'citizen_compensation_categories_grid',

            show: {
                toolbar: true,
                toolbarAdd: false,
                toolbarEdit: false,
                footer: true,
            },     

            toolbar: {
            
                items: [
                    {type: 'button', id: 'edit', caption: 'Импорт справочника из ГИС ЖКХ', onClick: $_DO.import_citizen_compensation_categories, icon: 'w2ui-icon-pencil', off: $_USER.role.nsi_20_10}
                ].filter (not_off),
                
            }, 

            searches: [
            ].filter (not_off),

            columns: [      
            
                {field: 'categoryname', caption: 'Наименование', size: 100},
                {field: 'vc_budget_level.label', caption: 'Бюджет', size: 20},
                {field: 'oktmo_label', caption: 'Территория', size: 20},
                {field: 'fromdate', caption: 'Дата начала', size: 20, render: _dt},
                {field: 'todate', caption: 'Дата окончания', size: 20, render: _dt},


            ].filter (not_off),
            
            url: '/_back/?type=citizen_compensation_categories',
            
            postData: {data: {}},
                        
            onAdd:      $_DO.create_citizen_compensation_categories,            
            onEdit:     $_DO.edit_citizen_compensation_categories,
            onDblClick: function (e) {openTab ('/citizen_compensation_category/' + e.recid)},
            onRefresh: function (e) {e.done (color_data_mandatory)}

        }).refresh ();

    }

})