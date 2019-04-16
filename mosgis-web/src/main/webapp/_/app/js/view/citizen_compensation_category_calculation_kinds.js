define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['topmost_layout'].el ('main')).w2regrid ({

            name: 'citizen_compensation_category_calculation_kinds_grid',

            show: {
                toolbar: true,
                toolbarAdd: false,
                toolbarEdit: false,
                toolbarInput:false,
                footer: true,
            },     

            toolbar: {
            
                items: [
                ].filter (not_off),
                
            }, 

            searches: [
            ].filter (not_off),

            columns: [      
            
                {field: 'vc_svc_types.label', caption: 'Услуга', size: 30},
                {field: 'vw_nsi_275.label', caption: 'Норматив', size: 50},
                {field: 'appliestoallfamilymembers', caption: 'Все члены семьи?', size: 20, voc: {0: 'нет', 1: 'да'}},
                {field: 'discountsize', caption: 'Размер компенсации, руб.', size: 30},
                {field: 'validfrom',  caption: 'Дата начала предоставления', size: 25, render: _dt},
                {field: 'validto', caption: 'Дата окончания предоставления', size: 25, render: _dt},
                {field: 'comment_', caption: 'Примечание', size: 50},


            ].filter (not_off),
            
            url: '/_back/?type=citizen_compensation_categories&part=calculation&id=' + $_REQUEST.id,

            onRefresh: function (e) {e.done (color_data_mandatory)}

        }).refresh ();

    }

})