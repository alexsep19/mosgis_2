define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')

        function canEdit () {
            
            if (!data.item.is_deleted && ($_USER.role.nsi_20_7 || $_USER.role.admin)) {
                switch (data.item.last_succesfull_status) {
                    case  10:
                    case -22:
                        return true
                }
            }
            return false

        }
        
        $(w2ui ['passport_layout'].el ('main')).w2regrid ({

            name: 'overhaul_regional_program_houses_grid',

            show: {
                toolbar: true,
                toolbarInput: false,
                toolbarAdd: canEdit (),
                toolbarDelete: canEdit (),
                footer: true,
            },

            columnGroups : [            
                {span: 2},                
                {span: 2, caption: 'Виды работ'}
            ],

            columns: [
                {field: 'address', caption: 'Адрес', size: 10, render: 
                    function (record) {
                        return record['address'] + ' (' + record['fiashouseguid'] + ')'
                }},
                {field: 'oktmo', caption: 'Код ОКТМО', size: 10},
                {field: 'works_general_cnt', caption: 'Всего', size: 5},
                {field: 'works_approved_cnt', caption: 'Размещено', size: 5},
            ],
            
            url: '/_back/?type=overhaul_regional_program_houses',

            postData: {program_uuid: $_REQUEST.id},

            onAdd: $_DO.create_overhaul_regional_program_house,
            onDelete: $_DO.delete_overhaul_regional_program_house,
            
            onDblClick: function (e) {
                openTab ('/overhaul_regional_program_house/' + e.recid)
            },

        }).refresh ();

    }

})