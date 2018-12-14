define ([], function () {

    var area_codes = {
        19924: 1,
        14532: 1,
        20145: 1,
    }

    $_DO.check_sum_area_fields_of_a_house = function () {
    
        var data = $('body').data ('data').item

        if (data.totalsquare == null) return
        totalsquare = parseFloat (data.totalsquare)

        var is_complete = true
        var sum = 0.0
        
        for (i in area_codes) {       
            var v = data ['f_' + i]
            if (v == null) is_complete = false; else sum += parseFloat (v)
        }

        var diff = sum - totalsquare
        var eps  = 1e-4
        
        function f (x) {return '(' + x.toFixed (4).replace (/0+$/, '').replace ('.', ',') + ' кв. м)'}
        
        function a (s) {
            alert ('Суммарная площадь здания на закладке "Общие"' + f (sum) + s + ' на закладке "Паспорт" ' + f (totalsquare))
        }

        if (is_complete) {
            if (Math.abs (diff) > eps) a (' отлична от значения, указанного')
        }
        else {
            if (diff > eps) a (' превышает значение, указанное')
        }

    }    
    
    $_DO.choose_tab_house = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('house.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) { 

        query ({type: 'houses'}, {}, function (data) {
        
            add_vocabularies (data, {
                vc_nsi_24: 1,
                vc_prop_doc_types: 1,
                vc_voting_forms: 1,
                vc_actions: 1,
                vc_gis_status: 1,
                vc_house_status: 1
            })
            
            data.active_tab = localStorage.getItem ('house.active_tab') || 'house_address'
            
            data.item.type = data.item.is_condo ? 'МКД' : 'ЖД'
            
            if ($_USER.role.admin || (data.cach && data.cach.is_own && data.cach.id_ctr_status_gis == 40)) data.is_passport_editable = 1
            
            data.depends = {

                20147: 17023,

                11053: 20169,
                11052: 20169,
                20817: 20169,
                20818: 20169,
                20024: 20169,

                20025: 20062,
                13026: 20062,
                14526: 20062,
                20170: 20062,

                20137: 20136,
                20138: 20136,
                20139: 20136,
                21819: 20136,

                20094: 11707,
                13207: 11707,
                16207: 11707,
                12035: 11707,
                20058: 11707,
                20048: 11707,
                20047: 11707,
                20046: 11707,
                20154: 11707,
                20050: 11707,
                20051: 11707,
                10221: 11707,
                20096: 11707,
                15055: 11707,
                14721: 11707,
                10291: 11707,
                20097: 11707,
                11791: 11707,
                12060: 11707,
                20098: 11707,
                20099: 11707,
                10536: 11707,
                20100: 11707,
                12036: 11707,
                20101: 11707,
                20102: 11707,
                20103: 11707,
                                
                20105: 11767,
                13267: 11767,
                11955: 11767,
                10278: 11767,
                20107: 11767,
                14778: 11767,
                10476: 11767,
                20108: 11767,
                11975: 11767,
                20109: 11767,
                20110: 11767,

                20112: 11745,
                14745: 11745,
                13477: 11745,
                20153: 11745,
                10524: 11745,
                20114: 11745,
                15024: 11745,
                16524: 11745,
                10478: 11745,
                20115: 11745,
                12023: 11745,
                20116: 11745,
                20117: 11745,
                
                20143: 11789,
                13289: 11789,
                13412: 11789,
                20045: 11789,

                20118: 11801,
                13301: 11801,
                16301: 11801,
                20057: 11801,
                20148: 11801,

                20122: 11665,
                12545: 11665,

            }
                                                
            $('body').data ('data', data)
            $('body').data ('area_codes', area_codes)

            done (data)        
            
        })
    }

})